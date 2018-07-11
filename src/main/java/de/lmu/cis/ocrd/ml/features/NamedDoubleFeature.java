package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public abstract class NamedDoubleFeature extends NamedFeature {
    protected NamedDoubleFeature(String name) {
        super(name);
    }

    @Override
    public final Object calculate(Token token, int i, int n) {
		final Double res = doCalculate(token, i, n);
//        if (res.isNaN() || res.isInfinite()) {
//        	throw new RuntimeException("token: " + token.toJSON() + " (" + i + ") (" + n + ") isNAN() or isInfinite()");
//		}
		return res;
    }

    protected abstract double doCalculate(Token token, int i, int n);
}
