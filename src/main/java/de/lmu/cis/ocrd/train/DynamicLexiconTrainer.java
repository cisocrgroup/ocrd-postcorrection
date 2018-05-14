package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DynamicLexiconTrainer {
    private final Environment environment;
    private final FeatureSet fs;

    public DynamicLexiconTrainer(Environment environment, FeatureSet fs) throws IOException {
        this.environment = environment.withDynamicLexiconFeatureSet(fs);
        this.fs = fs;
    }

    public void train() throws Exception {
        train(1);
        for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
            train(i + 2);
        }
    }

    private void train(int n) throws Exception {
        try (Writer w = new BufferedWriter(new FileWriter(environment.getDynamicLexiconTrainingFile(n).toFile()))) {
            final ARFFWriter arff = ARFFWriter.fromFeatureSet(fs)
                    .withRelation("DynamicLexiconExpansion_" + n)
                    .withWriter(w);
            // TODO: withDebugToken?
            arff.writeHeader(n);
            new Tokenizer(environment).eachToken((token) -> arff.writeFeatureVector(fs.calculateFeatureVector(token)));
            w.flush();
        }
    }
}
