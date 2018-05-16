package de.lmu.cis.ocrd.ml.features;

abstract class BaseFeatureImplementation implements Feature {
    protected static boolean handlesOnlyMasterOCR(int i, int ignored) {
        return i == 0;
    }
    protected static boolean handlesOnlyLastOtherOCR(int i, int n) {
        return (i+1) == n && i > 0;
    }
    protected static boolean handlesEveryOtherOCR(int i, int n) {
        return !handlesOnlyMasterOCR(i, n);
    }
    protected static boolean handlesAnyOCR(int i, int n) {
        return true;
    }
}
