package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.BinaryPrediction;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;
import org.pmw.tinylog.Logger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;
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

    private List<Result> predict(List<OCRToken> tokens, int n, InputStream is) throws Exception {
        LogisticClassifier classifier = LogisticClassifier.load(is);
        List<Result> results = new ArrayList<>(tokens.size());
        for (OCRToken token: TokenFilter.filter(tokens).collect(Collectors.toList())) {
            final FeatureSet.Vector values = featureSet.calculateFeatureVector(token, n);
            final Prediction prediction = classifier.predict(values);
            results.add(new Result(token, prediction));
        }
        return results;
    }

    public Map<OCRToken, List<Ranking>> getRankings(TokenReader tokenReader, Path rrModel, Path rrTrain) throws Exception {
        final LogisticClassifier classifier = LogisticClassifier.load(rrModel);
        final Instances instances = new ConverterUtils.DataSource(rrTrain.toString()).getDataSet();
        instances.setClassIndex(instances.numAttributes() - 1);

        final Iterator<Instance> iis = instances.iterator();
        final Iterator<OCRToken> tis = TokenFilter.filter(tokenReader.readTokens()).collect(Collectors.toList()).iterator();
        final Map<OCRToken, List<Ranking>> rankings = new HashMap<>();
        while (iis.hasNext() && tis.hasNext()) {
            final Instance i = iis.next();
            final OCRToken t = tis.next();
            for (Candidate candidate: t.getCandidates()) {
                final BinaryPrediction p = classifier.predict(i);
                final double ranking = p.getPrediction() ? p.getConfidence() : -p.getConfidence();
                if (Double.isNaN(ranking)) {
                    continue;
                }
                if (!rankings.containsKey(t)) {
                    rankings.put(t, new ArrayList<>());
                }
                rankings.get(t).add(new Ranking(candidate, ranking));
            }
            if (rankings.containsKey(t)) {
                rankings.get(t).sort((lhs, rhs) -> Double.compare(rhs.ranking, lhs.ranking));
            }
        }
        return rankings;
    }
}
