package de.lmu.cis.ocrd.ml.features;

abstract class NamedFeature extends BaseFeatureImplementation {
    private final String name;
    NamedFeature(String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }
}
