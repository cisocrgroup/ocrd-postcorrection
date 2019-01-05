package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateUnigramFeature extends UnigramFeature {
	private static final long serialVersionUID = 507211972850104646L;

	public CandidateUnigramFeature(JsonObject o, ArgumentFactory args) throws Exception {
		this(args, JSON.mustGetNameOrType(o));
	}

	private CandidateUnigramFeature(ArgumentFactory factory, String name) throws Exception {
		super(factory, name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}

	@Override
	public double doCalculate(OCRToken token, int i, int n) {
		return getUnigrams(i).getRelative(mustGetCandidate(token).Suggestion);
	}
}
