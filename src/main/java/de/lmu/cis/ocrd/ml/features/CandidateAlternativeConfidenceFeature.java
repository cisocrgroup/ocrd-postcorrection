package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.AbstractOCRToken;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;
import org.pmw.tinylog.Logger;

public class CandidateAlternativeConfidenceFeature extends NamedDoubleFeature {
    CandidateAlternativeConfidenceFeature(String name) {
        super(name);
    }

    public CandidateAlternativeConfidenceFeature(JsonObject o, ArgumentFactory args) {
        this(JSON.mustGetNameOrType(o));
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
        final String candidate = mustGetCandidate(token).Suggestion;
        if (token instanceof AbstractOCRToken) {
            return ((de.lmu.cis.ocrd.calamari.Token)((AbstractOCRToken)token).getBase()).getAlternativeConf(candidate);
        }
        return ((de.lmu.cis.ocrd.calamari.Token)token).getAlternativeConf(candidate);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesEverySlaveOCR(i, n);
    }
}
