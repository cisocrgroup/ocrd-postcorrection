package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.util.JSON;

import java.util.List;

public class HighestRankedCandidateDistanceToNextFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 5105792591821872162L;

	public HighestRankedCandidateDistanceToNextFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	public HighestRankedCandidateDistanceToNextFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final List<Candidate> cs = token.getAllProfilerCandidates();
		if (cs.size() < 2) {
			return 1.0;
		}
		return cs.get(0).Weight - cs.get(1).Weight;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
