package de.lmu.cis.ocrd.ml.features;

import java.util.List;

public abstract class NamedStringSetFeature extends NamedFeature {
    private final List<String> set;

    NamedStringSetFeature(String name, List<String> set) {
        super(name);
        this.set = set;
    }

    public final List<String> getSet() {
        return set;
    }
}
