package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

abstract class NamedBooleanFeature extends NamedFeature {
    NamedBooleanFeature(String name) {
        super(name);
    }

    @Override
    public final double calculate(Token token, int i, int n) {
        return this.doCalculate(token, i, n) ? 1 : -1;
    }

    abstract boolean doCalculate(Token token, int i, int n);
}
