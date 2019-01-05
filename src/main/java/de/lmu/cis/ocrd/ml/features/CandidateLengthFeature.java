package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateLengthFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 6518045049131388557L;

	public CandidateLengthFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	protected CandidateLengthFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		return mustGetCandidate(token).Suggestion.length();
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
