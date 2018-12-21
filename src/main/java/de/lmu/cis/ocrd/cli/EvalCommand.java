package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.pagexml.METS;
import org.pmw.tinylog.Logger;

import java.nio.file.Paths;
import java.util.List;

public class EvalCommand extends AbstractMLCommand {
	private METS mets;
	private AbstractMLCommand.Parameter parameter;

	@Override
	public String getName() {
		return "eval";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		parameter = getParameter(config);
		mets = METS.open(Paths.get(config.mustGetMETSFile()));
		for (String ifg : config.mustGetInputFileGroups()) {
			evaluate(ifg);
		}
	}

	private void evaluate(String ifg) {
		Logger.debug("evaluating input file group {}", ifg);
		for (int i = 0; i < parameter.nOCR; i++) {
			evaluate(mets.findFileGrpFiles(ifg), i, parameter.nOCR);
		}
	}

	private void evaluate(List<METS.File> files, int i, int n) {
		Logger.debug("evaluate({}, {}", i, n);
	}
}
