package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;

public class UnigramFeature extends NamedDoubleFeature {
    private final FreqMap<String> unigrams;

    public UnigramFeature(JsonObject o, ArgumentFactory args) {
        this(args.getMasterOCRUnigrams(), JSONUtil.mustGetNameOrType(o));
    }

    private UnigramFeature(FreqMap<String> unigrams, String name) {
        super(name);
        this.unigrams = unigrams;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyLastOtherOCR(i, n);
    }

    @Override
    public double doCalculate(Token token, int i, int n) {
        return unigrams.getRelative(token.getMasterOCR().toString());
    }
}
