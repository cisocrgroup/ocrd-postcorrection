package de.lmu.cis.ocrd.ml.features;

import java.util.List;

public abstract class NamedStringSetFeature extends NamedFeature {
	private static final long serialVersionUID = 7647450586760402681L;
	private final List<String> set;

	NamedStringSetFeature(String name, List<String> set) {
		super(name);
		this.set = set;
	}

	@Override
	public final String getClasses() {
		return String.format("{%s}", String.join(",", set));
	}

	public final List<String> getSet() {
		return set;
	}
}
