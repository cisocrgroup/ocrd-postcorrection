package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.*;
import org.apache.commons.lang.WordUtils;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class ProfilerCommand extends AbstractIOCommand {
	public static class Parameter {
		public String executable, backend, language;
		public List<String> args;
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		final Parameter parameter = config.mustGetParameter(Parameter.class);
		final String ifg = config.mustGetSingleInputFileGroup();
		final String ofg = config.mustGetSingleOutputFileGroup();
		final METS mets = METS.open(Paths.get(config.mustGetMETSFile()));
		final List<METS.File> files = mets.findFileGrpFiles(ifg);
		final Profile profile = makeProfiler(parameter, files).profile();
		for (METS.File file: files) {
			appendProfile(mets, file, profile, ofg);
		}
	}

	@Override
	public String getName() {
		return "profile";
	}

	private static void appendProfile(METS mets, METS.File file, Profile profile, String ofg) throws Exception {
		try (InputStream is = file.open()) {
			Page page = Page.parse(is);
			for (Line line: page.getLines()) {
				for (Word word: line.getWords()) {
					appendProfile(word, profile);
				}
			}
			addToWorkspace(mets, page, file, ofg);
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

	private static void addToWorkspace(METS mets, Page page, METS.File file, String ofg) {

	}

	private static Profiler makeProfiler(Parameter parameter, List<METS.File> files) throws Exception {
		return new FileGrpProfiler(files, makeProfilerProcess(parameter));
	}

	private static ProfilerProcess makeProfilerProcess(Parameter parameter) {
		return new LocalProfilerProcess(
				parameter.executable,
				Paths.get(parameter.backend, parameter.language+".ini")
		);
	}
}
