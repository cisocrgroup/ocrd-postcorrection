package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public abstract class NamedBooleanFeature extends NamedFeature {
    NamedBooleanFeature(String name) {
        super(name);
    }

    @Override
    public final Object calculate(Token token, int i, int n) {
        return this.doCalculate(token, i, n);
    }

    abstract boolean doCalculate(Token token, int i, int n);
}
