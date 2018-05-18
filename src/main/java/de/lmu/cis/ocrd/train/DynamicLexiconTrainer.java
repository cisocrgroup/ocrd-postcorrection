package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DynamicLexiconTrainer {
    private final Environment environment;
    private final FeatureSet fs;
    private int n;

    public DynamicLexiconTrainer(Environment environment, FeatureSet fs) throws IOException {
        this.environment = environment.withDynamicLexiconFeatureSet(fs);
        this.fs = fs.add(new DynamicLexiconGTFeature());
        this.n = 10;
    }

    public DynamicLexiconTrainer withSplitFraction(int n) {
        this.n = n;
        return this;
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
        final Path evalPath = environment.fullPath(environment.getDynamicLexiconTestFile(n));
        final List<Token> testTokens = new ArrayList<>();

        try (final Writer trainWriter = new BufferedWriter(new FileWriter(trainPath.toFile()))) {
            final ARFFWriter trainARFFWriter = ARFFWriter.fromFeatureSet(fs)
                    .withRelation("DynamicLexiconExpansion_" + n)
                    .withWriter(trainWriter)
                    .withDebugToken(environment.isDebugTokenAlignment());
            trainARFFWriter.writeHeader(n);
            final Tokenizer tokenizer = new Tokenizer(environment);
            new TrainSetSplitter(n).eachToken(tokenizer, (Token token, boolean isTrain) -> {
                if (isTrain) {
                    trainARFFWriter.writeToken(token);
                    trainARFFWriter.writeFeatureVector(fs.calculateFeatureVector(token));
                } else {
                    testTokens.add(token);
                }
            });
            trainWriter.flush();
        }
        try (final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(evalPath.toFile()))) {
            out.writeObject(testTokens);
            out.flush();
        }
    }

    private void train(int n) throws Exception {
        final Path trainingFile = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
        final ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(trainingFile.toString());
        final Instances train = dataSource.getDataSet();
        train.setClassIndex(train.numAttributes() - 1);
        final Classifier classifier = new Classifier(dataSource.getStructure());
        classifier.buildClassifier(train);
        final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFile.toFile()))) {
            out.writeObject(classifier);
            out.flush();
        }
    }

    private void evaluate(int n) throws Exception {
        final Classifier classifier = openClassifier(n);
        final List<Token> testTokens = openTestFile(n);
        for (Token token : testTokens) {
            System.out.println("predicting: " + token);
            final FeatureSet.Vector featureVector = fs.calculateFeatureVector(token);
            System.out.println("vector: " + featureVector);
            classifier.setClassIndex(featureVector.size() - 1);
            final double res = classifier.predict(featureVector);
            System.out.printf("prediction: %f\n", res);
        }
    }

    private Classifier openClassifier(int n) throws Exception {
        final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile.toFile()))) {
            return (Classifier) in.readObject();
        }
    }

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
