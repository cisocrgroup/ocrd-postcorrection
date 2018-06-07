package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import org.pmw.tinylog.Logger;
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
		Logger.info("preparing {} OCRs", n);
        final Path trainPath = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
        final List<Token> testTokens = new ArrayList<>();
		final FeatureSet fs = newFeatureSet();
		try (final Writer trainWriter = new BufferedWriter(new FileWriter(trainPath.toFile()))) {
			final ARFFWriter trainARFFWriter = ARFFWriter.fromFeatureSet(fs)
					.withRelation("DynamicLexiconExpansion_train_" + n)
					.withWriter(trainWriter)
					.withDebugToken(environment.openConfiguration().getDynamicLexiconTrainig().isDebugTrainingTokens());
			trainARFFWriter.writeHeader(n);
			newTrainSetSplitter().eachToken((Token token, boolean isTrain) -> {
				Logger.debug(token.toJSON());
				if (isTrain) {
					trainARFFWriter.writeToken(token);
					final FeatureSet.Vector v = fs.calculateFeatureVector(token, n);
					Logger.debug(v);
					trainARFFWriter.writeFeatureVector(v);
				} else {
					testTokens.add(token);
				}
            });
            trainWriter.flush();
        }
		final Path testPath = environment.fullPath(environment.getDynamicLexiconTestFile(n));
		try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(testPath.toFile()))) {
            out.writeObject(testTokens);
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
		Logger.info("training {} OCRs", n);
        final Path trainingFile = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
        final ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(trainingFile.toString());
        final Instances train = dataSource.getDataSet();
		final Instances structure = dataSource.getStructure();
        train.setClassIndex(train.numAttributes() - 1);
		final de.lmu.cis.ocrd.ml.Classifier classifier = LogisticClassifier.train(train, structure);
        final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFile.toFile()))) {
            out.writeObject(classifier);
            out.flush();
        }
    }

    private void evaluate(int n) throws Exception {
		Logger.info("evaluating {} OCRs", n);
		final de.lmu.cis.ocrd.ml.Classifier classifier = openClassifier(n);
        final List<Token> testTokens = openTestFile(n);
		final Path evaluationFile = environment.fullPath(environment.getDynamicLexiconEvaluationFile(n));
		final ErrorCounts errorCounts = new ErrorCounts();
		final FeatureSet fs = newFeatureSet();
		try (PrintWriter out = new PrintWriter(new FileOutputStream(evaluationFile.toFile()))) {
			for (Token token : testTokens) {
				printFormatted(out, "predicting: %s", token.toJSON());
				final FeatureSet.Vector featureVector = fs.calculateFeatureVector(token, n);
				printFormatted(out, "features: %s", featureVector.toJSON());
				// classifier.setClassIndex(featureVector.size() - 1);
				final Prediction prediction = classifier.predict(featureVector);
				printFormatted(out, "prediction: %s\n", prediction.toJSON());
				errorCounts.add(token, prediction, featureVector.get(featureVector.size() - 1));
			}
			for (Token token : errorCounts.getTruePositives()) {
				printFormatted(out, "correctly used for extension: %s", token.toJSON());
			}
			for (Token token : errorCounts.getFalsePositives()) {
				printFormatted(out, "incorrectly used for extension: %s", token.toJSON());
			}
			for (Token token : errorCounts.getFalseNegatives()) {
				printFormatted(out, "incorrectly not used for extension: %s", token.toJSON());
			}
			printFormatted(out, "Number of correctly used extensions:     %d", errorCounts.getTruePositiveCount());
			printFormatted(out, "Number of correctly unused extensions:   %d", errorCounts.getTrueNegativeCount());
			printFormatted(out, "Number of incorrectly used extensions:   %d", errorCounts.getFalsePositiveCount());
			printFormatted(out, "Number of incorrectly unused extensions: %d", errorCounts.getFalseNegativeCount());
			printFormatted(out, "Total number of tokens:                  %d", errorCounts.getTotalCount());
			out.flush();
        }
    }

	private static void printFormatted(Writer w, String fmt, Object... args) throws IOException {
		Logger.info(String.format(fmt, args));
		w.write(String.format(fmt, args));
	}

	private de.lmu.cis.ocrd.ml.Classifier openClassifier(int n) throws Exception {
        final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile.toFile()))) {
			return (de.lmu.cis.ocrd.ml.Classifier) in.readObject();
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
