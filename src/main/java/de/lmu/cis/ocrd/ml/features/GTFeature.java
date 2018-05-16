package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

// GTFeature is a feature that simply checks if the master OCR of any given token
// equals its ground-truth. This feature should be used to simply add GT-data to
// the training and evaluation steps.
public class GTFeature extends NamedBooleanFeature {
    public GTFeature() {
        super("GT");
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }

    @Override
    protected boolean doCalculate(Token token, int i, int n) {
        return token.isCorrectToken();
    }
}
