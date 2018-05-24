package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.cli.AsyncArgumentFactory;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profiler;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DynamicLexiconTrainer {
    private final Environment environment;
	private final Configuration configuration;

	public DynamicLexiconTrainer(Environment environment) throws IOException {
		this.environment = environment;
		this.configuration = environment.openConfiguration();
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
        final Path trainPath = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
        final List<Token> testTokens = new ArrayList<>();
		final FeatureSet fs = newFeatureSet();
		try (final Writer trainWriter = new BufferedWriter(new FileWriter(trainPath.toFile()))) {
			final ARFFWriter trainARFFWriter = ARFFWriter.fromFeatureSet(fs)
					.withRelation("DynamicLexiconExpansion_train_" + n)
					.withWriter(trainWriter)
					.withDebugToken(configuration.getDynamicLexiconTrainig().isDebugTrainingTokens());
			trainARFFWriter.writeHeader(n);
			newTrainSetSplitter().eachToken((Token token, boolean isTrain) -> {
				if (isTrain) {
					trainARFFWriter.writeToken(token);
					trainARFFWriter.writeFeatureVector(fs.calculateFeatureVector(token));
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
				.withArgumentFactory(new AsyncArgumentFactory(configuration, environment, newProfiler()))
				.createFeatureSet(configuration.getDynamicLexiconTrainig().getFeatures())
				.add(new DynamicLexiconGTFeature());
	}

	private Profiler newProfiler() {
		return new LocalProfiler()
				.withExecutable(configuration.getProfiler().getExecutable())
				.withLanguage(configuration.getProfiler().getLanguage())
				.withLanguageDirectory(configuration.getProfiler().getLanguageDirectory())
				.withArgs(configuration.getProfiler().getArguments())
				.withInputDocumentPath(environment.getMasterOCR());
	}

	private TrainSetSplitter newTrainSetSplitter() {
		final int n = configuration.getDynamicLexiconTrainig().getTestEvaluationFraction();
		final Tokenizer tokenizer = new Tokenizer(environment);
		return new TrainSetSplitter(tokenizer, n);
	}

    private void train(int n) throws Exception {
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
		final de.lmu.cis.ocrd.ml.Classifier classifier = openClassifier(n);
        final List<Token> testTokens = openTestFile(n);
		final Path evaluationFile = environment.fullPath(environment.getDynamicLexiconEvaluationFile(n));
		final ErrorCounts errorCounts = new ErrorCounts();
		final FeatureSet fs = newFeatureSet();
		try (PrintWriter out = new PrintWriter(new FileOutputStream(evaluationFile.toFile()))) {
			for (Token token : testTokens) {
				System.out.println("---");
				out.println("---");
				fprintf(out, "predicting: %s", token.toJSON());
				final FeatureSet.Vector featureVector = fs.calculateFeatureVector(token);
				fprintf(out, "features: %s", featureVector.toJSON());
				// classifier.setClassIndex(featureVector.size() - 1);
				final Prediction prediction = classifier.predict(featureVector);
				fprintf(out, "prediction: %s\n", prediction.toJSON());
				errorCounts.add(token, prediction, featureVector.get(featureVector.size() - 1));
			}
			for (Token token : errorCounts.getTruePositives()) {
				fprintf(out, "correctly used for extension: %s", token.toJSON());
			}
			for (Token token : errorCounts.getFalsePositives()) {
				fprintf(out, "incorrectly used for extension: %s", token.toJSON());
			}
			for (Token token : errorCounts.getFalseNegatives()) {
				fprintf(out, "incorrectly not used for extension: %s", token.toJSON());
			}
			fprintf(out, "Number of correctly used extensions:     %d", errorCounts.getTruePositiveCount());
			fprintf(out, "Number of correctly unused extensions:   %d", errorCounts.getTrueNegativeCount());
			fprintf(out, "Number of incorrectly used extensions:   %d", errorCounts.getFalsePositiveCount());
			fprintf(out, "Number of incorrectly unused extensions: %d", errorCounts.getFalseNegativeCount());
			out.flush();
        }
    }

	private static void fprintf(Writer w, String fmt, Object... args) throws IOException {
		System.out.println(String.format(fmt, args));
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
