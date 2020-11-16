package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateIsAlternativeFeature extends NamedBooleanFeature {
    CandidateIsAlternativeFeature(String name) {
        super(name);
    }

    public CandidateIsAlternativeFeature(JsonObject o, ArgumentFactory args) {
        this(JSON.mustGetNameOrType(o));
    }

    @Override
    boolean doCalculate(OCRToken token, int i, int n) {
        if (!(token instanceof de.lmu.cis.ocrd.calamari.Token)) {
            return false;
        }
        final String candidate = mustGetCandidate(token).Suggestion;
        final double conf = ((de.lmu.cis.ocrd.calamari.Token)token).getAlternativeConf(candidate);
        return conf > 0;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesEverySlaveOCR(i, n);
    }
}
