package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.Token;

public class TokenLengthFeature extends NamedBooleanFeature{
    private final int min, max;

    public TokenLengthFeature(JsonObject o, ArgumentFactory args) {
        this(JSONUtil.mustGet(o, "min").getAsInt(), JSONUtil.mustGet(o, "max").getAsInt(), JSONUtil.mustGetNameOrType(o));
    }

    // for testing
    public int getMin() {
        return min;
    }

    // for testing
    public int getMax() {
        return max;
    }

    public TokenLengthFeature(int min, int max, String name) {
        super(name);
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isAdditionalOCRFeature() {
        return false;
    }

    @Override
    protected final boolean doCalculate(Token token, int ignored) {
        final int n = token.getMasterOCR().toString().length();
        return n>=min && n <= max;
    }
}
