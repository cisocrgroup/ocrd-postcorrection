package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class SumOfMatchingAdditionalOCRFeature extends NamedFeature {
    public SumOfMatchingAdditionalOCRFeature(String name) {
        super(name);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        // return true for the last iteration of a OCR, if there are any additional OCRs
        // i.e: i=0, n=3 -> false, i=2, n=3 -> true
        // i=0, n=1 -> false
        return i+1 == n && i > 0;
    }

    @Override
    public double calculate(Token token, int i, int n) {
        assert(this.handlesOCR(i, n));
        double sum = 0;
        // i=0 is mater OCR
        for (int j = 1; j < n; j++) {
            if (token.getMasterOCR().toString().equals(token.getOtherOCRAt(j-1).toString())) {
                sum += 1;
            }
        }
        return sum;
    }
}
