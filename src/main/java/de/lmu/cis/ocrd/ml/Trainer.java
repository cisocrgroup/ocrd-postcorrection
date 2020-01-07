package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Trainer prepares an arff file and trains a model:
// 1. setup the trainer using withLM, withFeatureSet, ...
// 2. For each n:
//    1. call openARFFWriter
//    2. call prepare for input file groups (writes features to the arff writer)
//    3. call train (closes the arff writer and trains the model)
public class Trainer {
    private Map<String, TokenReader> tokenReaders; // map input file groups to readers
    private LM lm;
    private FeatureSet featureSet;
    private ARFFWriter arffWriter;
    private final TokenReaderFactory tokenReaderFactory;

    public Trainer(TokenReaderFactory tokenReaderFactory) {
        this.tokenReaderFactory = tokenReaderFactory;
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
                .withRelation(relation + "-" + n)
                .withWriter(writer)
                .writeHeader(n);
    }

    public void prepare(String ifg, int n) throws Exception {
        final List<OCRToken> tokens = readTokens(ifg);
        lm.setTokens(tokens);
        for (OCRToken token : tokens) {
            arffWriter.writeToken(token, n);
        }
    }

    public void train(Path arff, Path bin) throws Exception {
        arffWriter.close();
        LogisticClassifier classifier = LogisticClassifier.train(arff);
        classifier.save(bin);
    }

    private List<OCRToken> readTokens(String ifg) throws Exception {
        if (tokenReaders == null) {
            this.tokenReaders = new HashMap<>();
        }
        if (!tokenReaders.containsKey(ifg)) {
            tokenReaders.put(ifg, tokenReaderFactory.create(ifg));
        }
        return tokenReaders.get(ifg).readTokens();
    }
}
