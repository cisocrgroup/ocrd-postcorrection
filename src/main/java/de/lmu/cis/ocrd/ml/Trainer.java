package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.FeatureSet;
import org.pmw.tinylog.Logger;

import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

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

    public Trainer withFeatureSet(FeatureSet featureSet) {
        this.featureSet = featureSet;
        return this;
    }

    public Trainer withLanguageModel(LM lm) {
        this.lm = lm;
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

    public void closeARFFWriter() {
        arffWriter.close();
    }

    public void prepare(OCRTokenReader tokenReader, int n, TokenFilter.Func func) throws Exception {
        final List<OCRToken> tokens = tokenReader.read();
        lm.setTokens(tokens);
        TokenFilter.filter(tokens, (t)-> func.apply(t) && t.getGT().isPresent()).forEach(token->{
            Logger.debug("preparing {}: {}", arffWriter.getRelation(), token.toString());
            assert(TokenFilter.isNonLexical(token));
            assert(TokenFilter.isLong(token));
            assert(token.getGT().isPresent());
            arffWriter.writeToken(token, n);
        });
    }

    public void prepare(OCRTokenReader tokenReader, int n) throws Exception {
        final List<OCRToken> tokens = tokenReader.read();
        lm.setTokens(tokens);
        TokenFilter.filter(tokens, (t)-> t.getGT().isPresent()).forEach(token->{
            Logger.debug("preparing {}: {}", arffWriter.getRelation(), token.toString());
            assert(TokenFilter.isNonLexical(token));
            assert(TokenFilter.isLong(token));
            assert(token.getGT().isPresent());
            arffWriter.writeToken(token, n);
        });
    }

    public void train(Path arff, Path bin) throws Exception {
        Logger.info("train({}, {})", arff, bin);
        closeARFFWriter();
        LogisticClassifier classifier = LogisticClassifier.train(arff);
        classifier.save(bin);
    }
}
