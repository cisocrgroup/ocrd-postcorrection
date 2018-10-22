package de.lmu.cis.ocrd.ml.features;

public abstract class NamedBooleanFeature extends NamedFeature {
	NamedBooleanFeature(String name) {
		super(name);
	}

	@Override
	public final Object calculate(OCRToken token, int i, int n) {
		return this.doCalculate(token, i, n);
	}

	abstract boolean doCalculate(OCRToken token, int i, int n);
}
