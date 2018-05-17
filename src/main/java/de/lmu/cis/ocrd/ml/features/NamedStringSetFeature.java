package de.lmu.cis.ocrd.ml.features;

import java.util.Set;

public abstract class NamedStringSetFeature extends NamedFeature {
    private final Set<String> set;

    protected NamedStringSetFeature(String name, Set<String> set) {
        super(name);
        this.set = set;
    }

    public final Set<String> getSet() {
        return set;
    }
}
