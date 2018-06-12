package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import org.pmw.tinylog.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DynamicLexiconTrainer {
    private final Environment environment;

	public DynamicLexiconTrainer(Environment environment) {
		this.environment = environment;
	}

    public DynamicLexiconTrainer run() throws Exception {
        return prepare().train().evaluate();
    }

    public DynamicLexiconTrainer prepare() throws Exception {
		eachN(this::newPrepare);
        return this;
    }

    public DynamicLexiconTrainer train() throws Exception {
        eachN(this::train);
        return this;
    }

    public DynamicLexiconTrainer evaluate() throws Exception {
		eachN(this::newEvaluate);
        return this;
    }

//    private void prepare(int n) throws Exception {
//		Logger.info("preparing for {} OCR(s)", n);
//        final Path trainPath = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
//        final List<Token> testTokens = new ArrayList<>();
//		final FeatureSet fs = newFeatureSet();
//		final Profile profile = environment.getProfile();
//		try (final Writer trainWriter = new BufferedWriter(new FileWriter(trainPath.toFile()))) {
//			final ARFFWriter trainARFFWriter = ARFFWriter.fromFeatureSet(fs)
//					.withRelation("DynamicLexiconExpansion_train_" + n)
//					.withWriter(trainWriter)
//					.withDebugToken(environment.openConfiguration().getDynamicLexiconTrainig().isDebugTrainingTokens());
//			trainARFFWriter.writeHeader(n);
//			newTrainSetSplitter().eachToken((Token token, boolean isTrain) -> {
//				Logger.debug("token: {}, isTrain: {}", token, isTrain);
//				final String masterOCRWord = token.getMasterOCR().toString();
//				final Optional<Candidates> candidates = profile.get(masterOCRWord);
//				if (!candidates.isPresent() || candidates.get().Candidates.length == 0) {
//					Logger.debug("skipping: {}", token.toJSON());
//					return;
//				}
//				// Logger.info("Profile[{}]: {}", masterOCRWord, profile.get(masterOCRWord).isPresent());
//				if (isTrain) {
//					Logger.debug("training token: {}", token.toJSON());
//					trainARFFWriter.writeToken(token);
//					final FeatureSet.Vector v = fs.calculateFeatureVector(token, n);
//					Logger.debug(v);
//					trainARFFWriter.writeFeatureVector(v);
//					return;
//				}
//				Logger.debug("evaluation token: {}", token.toJSON());
//				testTokens.add(token);
//            });
//            trainWriter.flush();
//        }
//		final Path testPath = environment.fullPath(environment.getDynamicLexiconTestFile(n));
//		try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(testPath.toFile()))) {
//			out.writeObject(testTokens);
//			out.flush();
//		}
//    }

	private void newPrepare(int n) throws Exception {
		final Path trainPath = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
		final Path testPath = environment.fullPath(environment.getDynamicLexiconTestFile(n));
		final FeatureSet fs = newFeatureSet();
		final List<Token> tokens = new ArrayList<>();
		try (final TrainingAndTestARFFWriter w = new TrainingAndTestARFFWriter(fs, trainPath, testPath)) {
			w.withDebug(environment.openConfiguration().getDynamicLexiconTrainig().isDebugTrainingTokens());
			w.withRelationName("DynamicLexiconExpansion_" + n);
			w.writeHeader(n);
			newTrainSetSplitter().eachToken((Token token, boolean isTrain) -> {
				Logger.debug("token: {}, isTrain: {}", token, isTrain);
				w.add(token, isTrain);
				if (!isTrain) {
					tokens.add(token);
				}
			});
		}
		final Path tokensPath = environment.fullPath(environment.getDynamicLexiconTestTokensFile(n));
		try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tokensPath.toFile()))) {
			out.writeObject(tokens);
			out.flush();
		}
	}

	private FeatureSet newFeatureSet() throws Exception {
		return FeatureFactory.getDefault()
				.withArgumentFactory(environment)
				.createFeatureSet(environment.openConfiguration().getDynamicLexiconTrainig().getFeatures())
				.add(new DynamicLexiconGTFeature());
	}

	private TrainSetSplitter newTrainSetSplitter() throws IOException {
		final int n = environment.openConfiguration().getDynamicLexiconTrainig().getTestEvaluationFraction();
		final Tokenizer tokenizer = new Tokenizer(environment);
		return new TrainSetSplitter(tokenizer, n);
	}

    private void train(int n) throws Exception {
		Logger.info("training for {} OCR(s)", n);
		final Path trainingFile = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
		final ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(trainingFile.toString());
		final Instances train = dataSource.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);
		final Instances structure = dataSource.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier logistic = new SimpleLogistic();
		logistic.buildClassifier(train);
		final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFile.toFile()))) {
			out.writeObject(logistic);
            out.flush();
        }
    }

//    private void evaluate(int n) throws Exception {
//		Logger.info("evaluating {} OCR(s)", n);
//		final de.lmu.cis.ocrd.ml.Classifier classifier = openClassifier(n);
//        final List<Token> testTokens = openTestFile(n);
//		final Path evaluationFile = environment.fullPath(environment.getDynamicLexiconEvaluationFile(n));
//		final ErrorCounts errorCounts = new ErrorCounts();
//		final ErrorCounts typeErrorCounts = new ErrorCounts(true);
//		final FeatureSet fs = newFeatureSet();
//		try (PrintWriter out = new PrintWriter(new FileOutputStream(evaluationFile.toFile()))) {
//			for (Token token : testTokens) {
//				printFormatted(out, "predicting: %s", token.toJSON());
//				final FeatureSet.Vector featureVector = fs.calculateFeatureVector(token, n);
//				printFormatted(out, "features: %s", featureVector.toJSON());
//				// classifier.setClassIndex(featureVector.size() - 1);
//				final Prediction prediction = classifier.predict(featureVector);
//				printFormatted(out, "prediction: %s", prediction.toJSON());
//				errorCounts.add(token, prediction, featureVector.get(featureVector.size() - 1));
//				typeErrorCounts.add(token, prediction, featureVector.get(featureVector.size() - 1));
//			}
//			for (Token token : errorCounts.getTruePositives()) {
//				printFormatted(out, "correctly used for extension: %s", token.toJSON());
//			}
//			for (Token token : errorCounts.getFalsePositives()) {
//				printFormatted(out, "incorrectly used for extension: %s", token.toJSON());
//			}
//			for (Token token : errorCounts.getFalseNegatives()) {
//				printFormatted(out, "incorrectly not used for extension: %s", token.toJSON());
//			}
//			printFormatted(out, "Number of correctly used extensions:     %d", errorCounts.getTruePositiveCount());
//			printFormatted(out, "Number of correctly unused extensions:   %d", errorCounts.getTrueNegativeCount());
//			printFormatted(out, "Number of incorrectly used extensions:   %d", errorCounts.getFalsePositiveCount());
//			printFormatted(out, "Number of incorrectly unused extensions: %d", errorCounts.getFalseNegativeCount());
//			printFormatted(out, "Total number of tokens:                  %d", errorCounts.getTotalCount());
//			printFormatted(out, "Precision:                               %f", errorCounts.getPrecision());
//			printFormatted(out, "Recall:                                  %f", errorCounts.getRecall());
//			printFormatted(out, "F1:                                      %f", errorCounts.getF1());
//			printFormatted(out, "[types] Number of correctly used extensions:     %d", typeErrorCounts.getTruePositiveCount());
//			printFormatted(out, "[types] Number of correctly unused extensions:   %d", typeErrorCounts.getTrueNegativeCount());
//			printFormatted(out, "[types] Number of incorrectly used extensions:   %d", typeErrorCounts.getFalsePositiveCount());
//			printFormatted(out, "[types] Number of incorrectly unused extensions: %d", typeErrorCounts.getFalseNegativeCount());
//			printFormatted(out, "[types] Total number of types:                   %d", typeErrorCounts.getTotalCount());
//			printFormatted(out, "[types] Precision:                               %f", typeErrorCounts.getPrecision());
//			printFormatted(out, "[types] Recall:                                  %f", typeErrorCounts.getRecall());
//			printFormatted(out, "[types] F1:                                      %f", typeErrorCounts.getF1());
//			out.flush();
//        }
//    }

	private void newEvaluate(int n) throws Exception {
		Logger.info("evaluating {} OCR(s)", n);
		final Path testFile = environment.fullPath(environment.getDynamicLexiconTestFile(n));
		final ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(testFile.toString());
		final Instances test = dataSource.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);
		final Instances structure = dataSource.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier logistic = openClassifier(n);
		final Evaluation evaluation = new Evaluation(structure);
		evaluation.evaluateModel(logistic, test);
		System.out.println(evaluation.toSummaryString("\nResults\n=================\n", false));
		// final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
	}

	private static void printFormatted(Writer w, String fmt, Object... args) throws IOException {
		Logger.info(String.format(fmt, args));
		w.write(String.format(fmt, args) + "\n");
	}

	private AbstractClassifier openClassifier(int n) throws Exception {
        final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile.toFile()))) {
			return (SimpleLogistic) in.readObject();
        }
    }

	@SuppressWarnings("unchecked")
	private List<Token> openTestFile(int n) throws Exception {
		final Path testFile = environment.fullPath(environment.getDynamicLexiconTestFile(n));
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(testFile.toFile()))) {
			return (ArrayList<Token>) in.readObject();
		}
	}

    private interface EachNCallback {
        void apply(int n) throws Exception;
    }

    private void eachN(EachNCallback f) throws Exception {
       f.apply(1);
       for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
           f.apply(i+2);
       }
    }
}
