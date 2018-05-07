package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class TokenLengthFeature extends NamedFeature{
    private final int min, max;
    public TokenLengthFeature(int min, int max, String name) {
        super(name);
        this.min = min;
        this.max = max;
    }
    public double calculate(Token token) {
        final int n = token.getMasterOCR().toString().length();
        if (n >= min && n <= max) {
            return 1;
        }
        return 0;
    }
}
