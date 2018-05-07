package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;

public class UnigramFeature extends NamedFeature {
    private final FreqMap<String> unigrams;

    public UnigramFeature(FreqMap<String>unigrams, String name) {
        super(name);
        this.unigrams = unigrams;
    }

    @Override
    public double calculate(Token token) {
        return unigrams.getRelative(token.getMasterOCR().toString());
    }
}
