package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public abstract class NamedDoubleFeature extends NamedFeature {
    protected NamedDoubleFeature(String name) {
        super(name);
    }

    @Override
    public final Object calculate(Token token, int i, int n) {
        return this.doCalculate(token, i, n);
    }

    protected abstract double doCalculate(Token token, int i, int n);
}
