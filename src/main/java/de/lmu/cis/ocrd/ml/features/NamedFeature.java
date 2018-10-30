package de.lmu.cis.ocrd.ml.features;

abstract class NamedFeature extends BaseFeatureImplementation {
	private static final long serialVersionUID = -4851942231267570448L;
	private final String name;

	NamedFeature(String name) {
		this.name = name;
	}

	@Override
	public final String getName() {
		return name;
	}
}
