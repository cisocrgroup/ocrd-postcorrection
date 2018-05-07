package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class TokenLengthFeature extends NamedBooleanFeature{
    private final int min, max;

    public TokenLengthFeature(int min, int max, String name) {
        super(name);
        this.min = min;
        this.max = max;
    }

    @Override
    protected final boolean doCalculate(Token token) {
        final int n = token.getMasterOCR().toString().length();
        return n>=min && n <= max;
    }
}
