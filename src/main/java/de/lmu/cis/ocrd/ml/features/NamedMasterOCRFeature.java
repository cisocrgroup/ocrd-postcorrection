package de.lmu.cis.ocrd.ml.features;

public abstract class NamedMasterOCRFeature extends NamedFeature {

    public NamedMasterOCRFeature(String name) {
        super(name);
    }

    @Override
    public boolean handlesOCR(int i, int ignored) {
        return i == 0;
    }
}
