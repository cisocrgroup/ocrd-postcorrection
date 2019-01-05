package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateLengthClassFeature extends TokenLengthClassFeature {
	private static final long serialVersionUID = 1000888404407897300L;

	public CandidateLengthClassFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o), JSON.mustGet(o, "short").getAsInt(),
				JSON.mustGet(o, "medium").getAsInt(), JSON.mustGet(o, "long").getAsInt());
	}

	public CandidateLengthClassFeature(String name, int shrt, int medium, int lng) {
		super(name, shrt, medium, lng);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		return getLengthClass(mustGetCandidate(token).Suggestion.length());
	}
}
