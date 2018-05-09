package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

abstract class NamedBooleanFeature extends NamedFeature {
    NamedBooleanFeature(String name) {
        super(name);
    }

    @Override
    public final double calculate(Token token, int additionalOCR) {
        return this.doCalculate(token, additionalOCR) ? 1 : 0;
    }

    protected abstract boolean doCalculate(Token token, int additionalOCR);
}
