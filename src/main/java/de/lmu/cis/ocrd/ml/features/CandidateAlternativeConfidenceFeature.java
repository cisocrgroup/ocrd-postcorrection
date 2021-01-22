package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
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
        if (!(token instanceof de.lmu.cis.ocrd.calamari.Token)) {
            Logger.debug("NOT A CALAMARI TOKEN");
            return 0;
        }
        final String candidate = mustGetCandidate(token).Suggestion;
        return ((de.lmu.cis.ocrd.calamari.Token)token).getAlternativeConf(candidate);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesEverySlaveOCR(i, n);
    }
}
