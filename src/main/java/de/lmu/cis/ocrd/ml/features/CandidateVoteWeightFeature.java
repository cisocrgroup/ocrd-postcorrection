package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateVoteWeightFeature extends AbstractConfidenceFeature {
	private static final long serialVersionUID = -4415553562125497094L;

	public CandidateVoteWeightFeature(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o));
	}

	private CandidateVoteWeightFeature(String name) throws Exception {
		super(name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	protected double getConfidence(OCRToken token, int i, int n) {
		return mustGetCandidate(token).Weight;
	}
}
