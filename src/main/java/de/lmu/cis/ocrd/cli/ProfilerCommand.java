package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.*;
import org.apache.commons.lang.WordUtils;
import org.pmw.tinylog.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class ProfilerCommand extends AbstractIOCommand {
	public static class Parameter {
		String executable, backend, language;
		List<String> args;
	}

	private Workspace workspace;
	private Parameter parameter;
	private String ofg;

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		parameter = config.mustGetParameter(Parameter.class);
		ofg = config.mustGetSingleOutputFileGroup();
        workspace = new Workspace(Paths.get(config.mustGetMETSFile()));
        final String ifg = config.mustGetSingleInputFileGroup();
		final List<METS.File> files = workspace.getMETS().findFileGrpFiles(ifg);
		final Profile profile = makeProfiler(files).profile();
		for (METS.File file: files) {
			appendProfile(file, profile);
		}
		Logger.debug("writing mets file");
		workspace.save();
	}

	@Override
	public String getName() {
		return "profile";
	}

	private void appendProfile(METS.File file, Profile profile) throws Exception {
		try (InputStream is = file.open()) {
			Page page = Page.parse(is);
			for (Line line: page.getLines()) {
				for (Word word: line.getWords()) {
					appendProfile(word, profile);
				}
			}
			final Path ofile = workspace.putPageXML(page, ofg, file.getFLocat());
			Logger.debug("adding {} to workspace", ofile.toString());
		}
	}

	private static void appendProfile(Word word, Profile profile) {
		List<String> unicode = word.getUnicodeNormalized();
		if (unicode.isEmpty()) {
			return;
		}
		final String lower = unicode.get(0).toLowerCase();
		final Optional<Candidates> candidates = profile.get(lower);
		if (!candidates.isPresent()) {
			return;
		}
		final int n = unicode.size();
		for (int i = 0; i < candidates.get().Candidates.length; i++) {
			final Candidate candidate = candidates.get().Candidates[i];
			word.appendNewTextEquiv()
					.withConfidence(candidate.Weight)
					.withDataType(String.format("ocrd-cis-profiler-candidate-%d", i+1))
					.withIndex(i+n+1)
					.withDataTypeDetails(new Gson().toJson(candidate))
					.addUnicode(formatSuggestion(unicode.get(0), candidate.Suggestion));
		}
	}

	private static String formatSuggestion(String mOCR, String suggestion) {
		if (Character.getType(mOCR.codePointAt(0)) == Character.UPPERCASE_LETTER) {
			if (mOCR.codePoints().allMatch((c)-> Character.getType(c) == Character.UPPERCASE_LETTER)) {
				return suggestion.toUpperCase();
			}
			return WordUtils.capitalize(suggestion);
		}
		return suggestion.toLowerCase();
	}

	private Profiler makeProfiler(List<METS.File> files) {
		return new FileGrpProfiler(files, makeProfilerProcess());
	}

	private ProfilerProcess makeProfilerProcess() {
		return new LocalProfilerProcess(
				parameter.executable,
				Paths.get(parameter.backend, parameter.language+".ini")
		);
	}
}
