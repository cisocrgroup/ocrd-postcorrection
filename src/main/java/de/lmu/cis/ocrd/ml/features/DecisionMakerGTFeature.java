package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;

public class DecisionMakerGTFeature extends NamedStringSetFeature {
	public static DecisionMakerGTFeature create(int maxCandidates) {
		List<String> classes = new ArrayList<>(maxCandidates + 1);
		for (int i = 0; i <= maxCandidates; i++) {
			classes.add(Integer.toString(i));
		}
		return new DecisionMakerGTFeature(maxCandidates, classes);
	}

	private final int maxCandidates;

	private DecisionMakerGTFeature(int maxCandidates, List<String> classes) {
		super("DecisionMakerGT", classes);
		this.maxCandidates = maxCandidates;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		return getSet().get(selectCandidate(token));
	}

	private int selectCandidate(OCRToken token) {
		List<Candidate> candidates = token.getAllProfilerCandidates();
		final String gt = token.getGT().orElseThrow(()->new RuntimeException(
				"missing ground-truth"));
		for (int i = 0; i < maxCandidates && i < candidates.size(); i++) {
			if (gt.equals(candidates.get(i).Suggestion)) {
				return i+1;
			}
		}
		return 0;
	}
}
