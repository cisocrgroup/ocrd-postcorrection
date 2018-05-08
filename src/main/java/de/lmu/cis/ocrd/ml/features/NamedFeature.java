package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Feature;

abstract class NamedFeature implements Feature {
    private final String name;
    NamedFeature(String name) {
        this.name = name;
    }
    @Override
    public final String getName() {
        return name;
    }
}