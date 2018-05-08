package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;

public class UnigramFeature extends NamedFeature {
    private final FreqMap<String> unigrams;

    public UnigramFeature(JsonObject o, ArgumentFactory args) {
        this(args.getOCRUnigrams(), JSONUtil.mustGetNameOrType(o));
    }

    public UnigramFeature(FreqMap<String> unigrams, String name) {
        super(name);
        this.unigrams = unigrams;
    }

    @Override
    public double calculate(Token token) {
        return unigrams.getRelative(token.getMasterOCR().toString());
    }
}
