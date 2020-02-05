package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateOCRPatternConfidenceFeature extends AbstractPatternConfidenceFeature {
    public CandidateOCRPatternConfidenceFeature(JsonObject o, ArgumentFactory args) {
        this(JSON.mustGetNameOrType(o));
    }

    public CandidateOCRPatternConfidenceFeature(String name) {
        super(name);
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
        final Candidate candidate = mustGetCandidate(token);
        final OCRWord word = getWord(token, i, n);
        double ret = 1; // if the token does not have any ocr-patterns we just return 1.
        for (int j = 0; j < candidate.OCRPatterns.length; j++) {
            ret *= calculateConfidence(word, candidate.OCRPatterns[j]);
        }
        return ret;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }
}
