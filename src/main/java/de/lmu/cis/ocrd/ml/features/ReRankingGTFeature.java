package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.profile.Candidate;

import java.util.Optional;

public class ReRankingGTFeature extends NamedBooleanFeature {
	public ReRankingGTFeature(String name) {
		super(name);
	}

	public ReRankingGTFeature() {
		this("ReRankingGT");
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	boolean doCalculate(OCRToken token, int i, int n) {
		Optional<Candidate> cand = token.getProfilerCandidate();
		if (!cand.isPresent()) {
			return token.getGT().get().equals(token.getMasterOCR().toString());
		}
		return token.getGT().get().equals(cand.get().Suggestion);
	}
}
