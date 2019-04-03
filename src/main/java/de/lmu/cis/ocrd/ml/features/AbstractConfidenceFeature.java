package de.lmu.cis.ocrd.ml.features;

public abstract class AbstractConfidenceFeature extends NamedDoubleFeature {
    public final static double MIN = 1e-5;

    public AbstractConfidenceFeature(String name) {
        super(name);
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
        final double conf = getConfidence(token, i, n);
        if (conf <= 0) {
            return MIN;
        }
        if (conf > 1) {
            return 1;
        }
        return conf;
    }

    protected abstract double getConfidence(OCRToken token, int i, int n);
}
