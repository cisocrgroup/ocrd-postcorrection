package de.lmu.cis.ocrd.train;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class DynamicLexiconTrainer {
	private final Environment environment;

	public DynamicLexiconTrainer(Environment environment) {
		this.environment = environment;
	}

	public DynamicLexiconTrainer run() throws Exception {
		return prepare().train().evaluate();
	}

	public DynamicLexiconTrainer prepare() throws Exception {
		eachN(this::prepare);
		return this;
	}

	public DynamicLexiconTrainer train() throws Exception {
		eachN(this::train);
		return this;
	}

	public DynamicLexiconTrainer evaluate() throws Exception {
		eachN(this::evaluate);
		return this;
	}

	private void prepare(int n) throws Exception {
		final Path trainPath = environment
				.fullPath(environment.getDynamicLexiconTrainingFile(n));
		final Path testPath = environment
				.fullPath(environment.getDynamicLexiconTestFile(n));
		final FeatureSet fs = newFeatureSet();
		final List<Token> tokens = new ArrayList<>();
		try (final TrainingAndTestARFFWriter w = new TrainingAndTestARFFWriter(
				fs, trainPath, testPath)) {
			w.withDebug(environment.openConfiguration()
					.getDynamicLexiconTrainig().isDebugTrainingTokens());
			w.withRelationName("DynamicLexiconExpansion_" + n);
			w.writeHeader(n);
			newTrainSetSplitter().eachToken((Token token, boolean isTrain) -> {
				Logger.debug("token: {}, isTrain: {}", token, isTrain);
				if (token.getMasterOCR().isShort()) {
					Logger.debug("SKIPPING (small)");
				}
				w.add(token, isTrain);
				if (!isTrain) {
					tokens.add(token);
				}
			});
		}
		final Path tokensPath = environment
				.fullPath(environment.getDynamicLexiconTestTokensFile(n));
		try (final ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(tokensPath.toFile()))) {
			out.writeObject(tokens);
			out.flush();
		}
	}

	private FeatureSet newFeatureSet() throws Exception {
		return FeatureFactory.getDefault().withArgumentFactory(environment)
				.createFeatureSet(environment.openConfiguration()
						.getDynamicLexiconTrainig().getFeatures())
				.add(new DynamicLexiconGTFeature());
	}

	private TrainSetSplitter newTrainSetSplitter() throws IOException {
		final int n = environment.openConfiguration().getDynamicLexiconTrainig()
				.getTestEvaluationFraction();
		final Tokenizer tokenizer = new Tokenizer(environment);
		return new TrainSetSplitter(tokenizer, n);
	}

	private void train(int n) throws Exception {
		Logger.info("training for {} OCR(s)", n);
		final Path trainingFile = environment
				.fullPath(environment.getDynamicLexiconTrainingFile(n));
		final ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(
				trainingFile.toString());
		final Instances train = dataSource.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);
		final Instances structure = dataSource.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier logistic = new SimpleLogistic();
		logistic.buildClassifier(train);
		final Path modelFile = environment
				.fullPath(environment.getDynamicLexiconModel(n));
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(modelFile.toFile()))) {
			out.writeObject(logistic);
			out.flush();
		}
	}

	private void evaluate(int n) throws Exception {
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
		}

	}

	private AbstractClassifier openClassifier(int n) throws Exception {
		final Path modelFile = environment
				.fullPath(environment.getDynamicLexiconModel(n));
		try (ObjectInputStream in = new ObjectInputStream(
				new FileInputStream(modelFile.toFile()))) {
			return (SimpleLogistic) in.readObject();
		}
	}

	private void eachN(EachNCallback f) throws Exception {
		f.apply(1);
		for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
			f.apply(i + 2);
		}
	}

	private interface EachNCallback {
		void apply(int n) throws Exception;
	}
}
