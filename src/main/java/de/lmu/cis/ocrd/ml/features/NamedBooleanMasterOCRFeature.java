package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public abstract class NamedBooleanMasterOCRFeature extends NamedMasterOCRFeature {
    NamedBooleanMasterOCRFeature(String name) {
        super(name);
    }

    @Override
    public final double calculate(Token token, int ignored1, int ignored2) {
        return this.doCalculate(token) ? 1 : -1;
    }

    protected abstract boolean doCalculate(Token token);
}
