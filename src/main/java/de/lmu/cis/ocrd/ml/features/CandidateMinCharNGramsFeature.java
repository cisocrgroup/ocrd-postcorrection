package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateMinCharNGramsFeature extends MinCharNGramsFeature {
	private static final long serialVersionUID = 18876765L;

	public CandidateMinCharNGramsFeature(JsonObject o, ArgumentFactory factory) throws Exception {
		this(JSON.mustGetNameOrType(o), factory.getCharacterTrigrams());
	}

	public CandidateMinCharNGramsFeature(String name, FreqMap ngrams) {
		super(name, ngrams);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		return getMinCharNGram(mustGetCandidate(token).Suggestion);
	}
}
