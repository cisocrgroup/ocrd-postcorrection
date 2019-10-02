package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.pagexml.FileGrpProfiler;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Workspace;
import de.lmu.cis.ocrd.profile.*;
import org.pmw.tinylog.Logger;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProfilerCommand extends AbstractIOCommand {
	public static class Parameter {
		String executable ="";
		String backend = "";
		String language = "";
		String additionalLexicon = "";
	}

	private Workspace workspace;
	private Parameter parameter;

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		config.setCommand(this);
		parameter = config.mustGetParameter(Parameter.class);
		final String ifg = config.mustGetSingleInputFileGroup();
		final String ofg = config.mustGetSingleOutputFileGroup();
        workspace = new Workspace(Paths.get(config.mustGetMETSFile()));
		final List<METS.File> files = workspace.getMETS().findFileGrpFiles(ifg);
		final Profile profile = makeProfiler(files).profile();
		Logger.debug("storing profile in workspace");
		workspace.putProfile(profile, ofg);
		Logger.debug("writing mets file");
		workspace.save();
	}

	@Override
	public String getName() {
		return "profile";
	}

	private Profiler makeProfiler(List<METS.File> files) throws Exception {
		List<Page> pages = new ArrayList<>(files.size());
		for (METS.File file: files) {
			try (InputStream is = file.openInputStream()) {
				pages.add(Page.parse(Paths.get(file.getFLocat()), is));
			}
		}
		return new FileGrpProfiler(pages, makeProfilerProcess());
	}

	private ProfilerProcess makeProfilerProcess() {
		AdditionalLexicon alex = new NoAdditionalLexicon();
		if (parameter.additionalLexicon != null && !"".equals(parameter.additionalLexicon)) {
			alex = new AdditionalFileLexicon(Paths.get(parameter.additionalLexicon));
		}
		return new LocalProfilerProcess(
				Paths.get(parameter.executable),
				Paths.get(parameter.backend, parameter.language+".ini"),
				alex
		);
	}
}
