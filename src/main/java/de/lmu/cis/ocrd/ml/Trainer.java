package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.FeatureSet;
import org.pmw.tinylog.Logger;

import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Trainer prepares an arff file and trains a model; must be called in this sequence:
// 1. setup the trainer using withLM, withFeatureSet, ...
// 2. For each n:
//    1. call openARFFWriter
//    2. call prepare for input file groups (writes features to the arff writer)
//    3. call train (closes the arff writer and trains the model)
public class Trainer {
    private LM lm;
    private FeatureSet featureSet;
    private ARFFWriter arffWriter;
    private boolean debug;

    public Trainer withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public Trainer withLM(LM lm) {
        this.lm = lm;
        return this;
    }

    public Trainer withFeatureSet(FeatureSet featureSet) {
        this.featureSet = featureSet;
        return this;
    }

    public void openARFFWriter(Writer writer, String relation, int n) {
        this.arffWriter = ARFFWriter
                .fromFeatureSet(featureSet)
                .withDebugToken(debug)
                .withRelation(relation + "-" + n)
                .withWriter(writer)
                .writeHeader(n);
    }

    public void prepare(TokenReader tokenReader, int n) throws Exception {
        final List<OCRToken> tokens = tokenReader.readTokens();
        lm.setTokens(tokens);
        TokenFilter.filter(tokens).forEach(token->{
            Logger.debug("preparing {}: {}", arffWriter.getRelation(), token.toString());
            arffWriter.writeToken(token, n);
        });
    }

    public void train(Path arff, Path bin) throws Exception {
        Logger.info("train({}, {})", arff, bin);
        arffWriter.close();
        LogisticClassifier classifier = LogisticClassifier.train(arff);
        classifier.save(bin);
    }

    public static class Result {
        private final OCRToken token;
        private final Prediction prediction;

        private Result(OCRToken token, Prediction prediction) {
            this.token = token;
            this.prediction = prediction;
        }

        public OCRToken getToken() {
            return token;
        }

        public Prediction getPrediction() {
            return prediction;
        }
    }

    private List<Result> predict(List<OCRToken> tokens, int n, Path bin) throws Exception {
        LogisticClassifier classifier = LogisticClassifier.load(bin);
        List<Result> results = new ArrayList<>(tokens.size());
        for (OCRToken token: TokenFilter.filter(tokens).collect(Collectors.toList())) {
            final FeatureSet.Vector values = featureSet.calculateFeatureVector(token, n);
            final Prediction prediction = classifier.predict(values);
            results.add(new Result(token, prediction));
        }
        return results;
    }
}
