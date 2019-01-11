package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.util.JSON;

import java.util.List;

public class HighestRankedCandidateVoteWeightFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = -4415553562125497094L;

	public HighestRankedCandidateVoteWeightFeature(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o));
	}

	public HighestRankedCandidateVoteWeightFeature(String name) throws Exception {
		super(name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final List<Candidate> cs  = token.getAllProfilerCandidates();
		return cs.size() > 0 ? cs.get(0).Weight : 1.0;
	}
}
