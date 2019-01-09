package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateMatchesOCRTokenFeature extends NamedBooleanFeature {
	private static final long serialVersionUID = -3415553562125497094L;

	public CandidateMatchesOCRTokenFeature(JsonObject o,
	                                       ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o));
	}

	public CandidateMatchesOCRTokenFeature(String name) {
		super(name);
	}

	@Override
	boolean doCalculate(OCRToken token, int i, int n) {
		return getWord(token, i, n).toString().equals(mustGetCandidate(token).Suggestion);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}
}
