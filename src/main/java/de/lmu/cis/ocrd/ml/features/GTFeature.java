package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

// GTFeature is a feature that simply checks if the master OCR of any given token
// equals its ground-truth. This feature should be used to simply add GT-data to
// the training and evaluation steps.
public class GTFeature extends NamedFeature {
    public GTFeature() {
        super("GT");
    }
    @Override
    public double calculate(Token token) {
        if (token.getGT().isPresent() && token.getGT().get().equals(token.getMasterOCR())) {
            return 1.0;
        }
        return 0.0;
    }
}
