package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class IsLongerThan extends NamedFeature{
    private final int n;
    public IsLongerThan(int n, String name) {
        super(name);
        this.n = n;
    }
    public double calculate(Token token) {
        if (token.getMasterOCR().length() > n) {
            return 1;
        }
        return 0;
    }
}
