package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.FeatureSet;

import java.io.InputStream;

public class Predictor {
    private FeatureSet featureSet;
    private LM lm;
    private LogisticClassifier classifier;

    public Predictor withFeatureSet(FeatureSet featureSet) {
        this.featureSet = featureSet;
        return this;
    }

    public Predictor withLanguageModel(LM lm) {
        this.lm = lm;
        return this;
    }

    public Predictor withOpenClassifier(InputStream is) throws Exception {
        classifier = LogisticClassifier.load(is);
        return this;
    }

    public Predictor withTokens(TokenReader tokenReader) throws Exception {
        lm.setTokens(tokenReader.readTokens());
        return this;
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

    public Result predict(OCRToken token, int n) throws Exception {
        final FeatureSet.Vector values = featureSet.calculateFeatureVector(token, n);
        final Prediction prediction = classifier.predict(values);
        return new Result(token, prediction);
    }
}
