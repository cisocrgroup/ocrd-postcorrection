package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.FeatureSet;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Predictor {
    private FeatureSet featureSet;
    private LM lm;

    public Predictor withFeatureSet(FeatureSet featureSet) {
        this.featureSet = featureSet;
        return this;
    }

    public Predictor withLanguageModel(LM lm) {
        this.lm = lm;
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

    public List<Result> predict(TokenReader tokenReader, int n, Path bin) throws Exception {
        List<OCRToken> tokens = tokenReader.readTokens();
        lm.setTokens(tokens);
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
