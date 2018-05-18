package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.file.Path;

public class DynamicLexiconTrainer {
    private final Environment environment;
    private final FeatureSet fs;
    private AbstractClassifier classifier;
    private int n;

    public DynamicLexiconTrainer(Environment environment, FeatureSet fs) throws IOException {
        this.environment = environment.withDynamicLexiconFeatureSet(fs);
        this.fs = fs.add(new DynamicLexiconGTFeature());
        this.classifier = new Logistic();
        this.n = 10;
    }

    public DynamicLexiconTrainer withClassifier(AbstractClassifier classififer) {
        this.classifier = classififer;
        return this;
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
        final Path evalPath = environment.fullPath(environment.getDynamicLexiconEvaluationFile(n));
        try (final Writer trainWriter = new BufferedWriter(new FileWriter(trainPath.toFile()));
             final Writer evalWriter = new BufferedWriter(new FileWriter(evalPath.toFile()))) {
            final ARFFWriter trainARFFWriter = ARFFWriter.fromFeatureSet(fs)
                    .withRelation("DynamicLexiconExpansion_" + n)
                    .withWriter(trainWriter)
                    .withDebugToken(environment.isDebugTokenAlignment());
            final ARFFWriter evalARFFWriter = ARFFWriter.fromFeatureSet(fs)
                    .withRelation("DynamicLexiconExpansion_" + n)
                    .withWriter(evalWriter)
                    .withDebugToken(environment.isDebugTokenAlignment());
            trainARFFWriter.writeHeader(n);
            evalARFFWriter.writeHeader(n);
            final Tokenizer tokenizer = new Tokenizer(environment);
            new TrainSetSplitter(n).eachToken(tokenizer, (Token token, boolean isTrain) -> {
                ARFFWriter w = isTrain ? trainARFFWriter : evalARFFWriter;
                w.writeToken(token);
                w.writeFeatureVector(fs.calculateFeatureVector(token));
            });
            trainWriter.flush();
            evalWriter.flush();
        }
    }

    private void train(int n) throws Exception {
        final Path trainingFile = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
        final Instances train = new ConverterUtils.DataSource(trainingFile.toString()).getDataSet();
        train.setClassIndex(train.numAttributes() - 1);
        classifier.buildClassifier(train);
        final Path modelFile = environment.fullPath(environment.getDynamicLexiconModel(n));
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFile.toFile()))) {
            out.writeObject(classifier);
            out.flush();
        }
    }

    private void evaluate(int n) {

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
