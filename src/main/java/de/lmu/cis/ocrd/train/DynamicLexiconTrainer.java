package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class DynamicLexiconTrainer {
    private final Environment environment;
    private final FeatureSet fs;

    public DynamicLexiconTrainer(Environment environment, FeatureSet fs) throws IOException {
        this.environment = environment.withDynamicLexiconFeatureSet(fs);
        this.fs = fs;
    }

    public void train() throws Exception {
        eachN(this::writeARFFiles);
        trainModels();
    }

    private void writeARFFiles(int n) throws Exception {
        final Path f = environment.fullPath(environment.getDynamicLexiconTrainingFile(n));
        try (Writer w = new BufferedWriter(new FileWriter(f.toFile()))) {
            final ARFFWriter arff = ARFFWriter.fromFeatureSet(fs)
                    .withRelation("DynamicLexiconExpansion_" + n)
                    .withWriter(w)
                    .withDebugToken(environment.isDebugTokenAlignment());
            arff.writeHeader(n);
            new Tokenizer(environment).eachToken((token) -> {
                arff.writeToken(token);
                arff.writeFeatureVector(fs.calculateFeatureVector(token));
            });
            w.flush();
        }
    }

    private void trainModels() {

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
