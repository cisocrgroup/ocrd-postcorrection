package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.Candidate;

public class ReRankingGTFeature extends NamedBooleanFeature {
	private ReRankingGTFeature(String name) {
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
		Candidate candidate = mustGetCandidate(token);
		return token.getGT().orElseThrow(RuntimeException::new).toLowerCase().equals(candidate.Suggestion.toLowerCase());
	}
}
