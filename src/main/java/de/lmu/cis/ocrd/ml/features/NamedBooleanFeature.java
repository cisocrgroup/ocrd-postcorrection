package de.lmu.cis.ocrd.ml.features;

public abstract class NamedBooleanFeature extends NamedFeature {
	private static final long serialVersionUID = 2760200162824149331L;

	NamedBooleanFeature(String name) {
		super(name);
	}

	@Override
	public final Object calculate(OCRToken token, int i, int n) {
		return this.doCalculate(token, i, n);
	}

	@Override
	public final String getClasses() {
		return String.format("{%s,%s}", Boolean.toString(true), Boolean.toString(false));
	}

	abstract boolean doCalculate(OCRToken token, int i, int n);
}
