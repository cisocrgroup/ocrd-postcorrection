package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;

public abstract class NamedDoubleFeature extends NamedFeature {
	private static final long serialVersionUID = -2596016620596603718L;

	protected NamedDoubleFeature(String name) {
		super(name);
	}

	@Override
	public final Object calculate(OCRToken token, int i, int n) {
		final Double res = doCalculate(token, i, n);
        if (res.isNaN() || res.isInfinite()) {
        	throw new RuntimeException("token: " + token.toString() + " (" + i + ") (" + n + ") isNAN() or isInfinite()");
		}
		return res;
	}

	@Override
	public final String getClasses() {
		return "REAL";
	}

	protected abstract double doCalculate(OCRToken token, int i, int n);
}
