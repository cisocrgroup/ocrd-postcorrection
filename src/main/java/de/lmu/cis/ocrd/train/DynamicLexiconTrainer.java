package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.FeatureSet;

import java.io.IOException;

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
        new Tokenizer(environment).eachToken((token)->{

        });
    }
}
