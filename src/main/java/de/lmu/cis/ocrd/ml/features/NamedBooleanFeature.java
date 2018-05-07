package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public abstract class NamedBooleanFeature extends NamedFeature {
    NamedBooleanFeature(String name) {
        super(name);
    }

    @Override
    public final double calculate(Token token) {
        return this.doCalculate(token) ? 1 : 0;
    }

    protected abstract boolean doCalculate(Token token);
}
