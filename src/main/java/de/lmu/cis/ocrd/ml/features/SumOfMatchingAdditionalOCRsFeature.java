package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;

public class SumOfMatchingAdditionalOCRsFeature extends NamedDoubleFeature {
    public SumOfMatchingAdditionalOCRsFeature(JsonObject o, ArgumentFactory factory) {
        this(JSONUtil.mustGetNameOrType(o));
    }

    public SumOfMatchingAdditionalOCRsFeature(String name) {
        super(name);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyLastOtherOCR(i, n);
    }

    @Override
    public double doCalculate(Token token, int i, int n) {
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
