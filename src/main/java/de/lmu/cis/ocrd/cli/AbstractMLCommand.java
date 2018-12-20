package de.lmu.cis.ocrd.cli;

abstract class AbstractMLCommand implements Command {

	static class TrainingResource {
		String model = "", training = "", features = "";
	}

	static class DLETrainingResource extends TrainingResource {
		public String dynamicLexicon = "";
	}

	static class Parameter {
		DLETrainingResource dleTraining;
		TrainingResource rrTraining, dmTraining;
		String trigrams = "";
		int nOCR;
	}

	protected Parameter getParameter(CommandLineArguments args) throws Exception {
		return args.mustGetParameter(Parameter.class);
	}
}
