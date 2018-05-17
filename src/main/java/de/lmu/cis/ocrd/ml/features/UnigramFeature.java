package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;

public class UnigramFeature extends NamedDoubleFeature {
    private final ArgumentFactory factory;

    public UnigramFeature(JsonObject o, ArgumentFactory args) {
        this(args, JSONUtil.mustGetNameOrType(o));
    }

    private UnigramFeature(ArgumentFactory factory, String name) {
        super(name);
        this.factory = factory;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesAnyOCR(i, n);
    }

    @Override
    public double doCalculate(Token token, int i, int n) {
        if (i == 0) {
            return factory.getMasterOCRUnigrams().getRelative(getWord(token, i, n).toString());
        }
        return factory.getOtherOCRUnigrams(i - 1).getAbsolute(getWord(token, i, n).toString());
    }
}
