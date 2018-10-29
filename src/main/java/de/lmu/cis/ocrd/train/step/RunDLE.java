package de.lmu.cis.ocrd.train.step;

import java.io.IOException;
import java.util.List;

public class RunDLE extends Base {
	public static RunDLE create(String[] args) throws IOException {
		boolean gt = args[0].toLowerCase().equals("true");
		return new RunDLE(gt, args[1], args[2], args[3], new ModelDir(args[4]),
				Base.toList(args, 5));
	}

	private RunDLE(boolean gt, String logLevel, String profile, String trigrams,
			ModelDir dir, List<String> files) throws IOException {
		super(gt, logLevel, profile, trigrams, dir, files);
	}

	public void run() {
		recognize();
		if (isWithGT()) {
			evaluate();
		}
	}

	private void recognize() {

	}

	private void evaluate() {
		/*
		Logger.info("evaluating {} OCR(s)", n);
		final Path testFile = environment
				.fullPath(environment.getDynamicLexiconTestFile(n));
		final ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(
				testFile.toString());
		final Instances test = dataSource.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);
		final Instances structure = dataSource.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier logistic = openClassifier(n);
		final Evaluation evaluation = new Evaluation(structure);
		evaluation.evaluateModel(logistic, test);
		final Path evaluationFile = environment
				.fullPath(environment.getDynamicLexiconEvaluationFile(n));
		try (final Writer w = new BufferedWriter(
				new FileWriter(evaluationFile.toString()))) {
			final String summary = evaluation.toSummaryString(
					String.format("\n Results (%d)\n=============\n", n), true);
			w.write(summary);
			w.write('\n');
			System.out.println(summary);
		*/

	}

	public static void main(String[] args) throws Exception {
		if (args.length < 5) {
			throw new Exception(
					"Usage: [true|false] logLevel profile trigrams dir files...");
		}
		if (!args[0].toLowerCase().equals("false")
				&& !args[0].toLowerCase().equals("true")) {
			throw new Exception(
					"Usage: [true|false] logLevel profile trigrams dir files...");
		}
		RunDLE.create(args).run();
	}
}
