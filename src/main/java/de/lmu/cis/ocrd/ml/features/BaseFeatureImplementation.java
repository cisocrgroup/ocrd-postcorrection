package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.ml.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.List;
import java.util.Optional;

abstract class BaseFeatureImplementation implements Feature {
	private static final long serialVersionUID = 1L;

	protected static boolean handlesOnlyMasterOCR(int i, int ignored) {
		return i == 0;
	}

	static boolean handlesOnlyLastOtherOCR(int i, int n) {
		return (i + 1) == n && i > 0;
	}

	static boolean handlesEverySlaveOCR(int i, int n) {
		return !handlesOnlyMasterOCR(i, n);
	}

	static boolean handlesExactlyOCR(int ocr, int i, int n) {
		return ocr == i;
	}

	static boolean handlesAnyOCR(int i, int n) {
		return true;
	}

	final OCRWord getWord(OCRToken token, int i, int n) {
		assert (i >= 0);
		assert (handlesOCR(i, n));
		if (i == 0) { // master OCR
			return token.getMasterOCR();
		}
		return token.getSlaveOCR(i - 1);
	}

	// return either the best weighted candidate (by the profiler), the according candidate (candidate tokens for ranking),
	// or the best ranked candidate (by the ranker).
	protected Candidate mustGetCandidate(OCRToken token) {
		// candidate token
		final Optional<Candidate> candidate = token.getCandidate();
		if (candidate.isPresent()) {
			return candidate.get();
		}
		// profiler candidates
		final List<Candidate> candidates = token.getCandidates();
		if (!candidates.isEmpty()) {
			return candidates.get(0);
		}
		// ranked candidates
		List<Ranking> rankings = token.getRankings();
		if (!rankings.isEmpty()) {
			return rankings.get(0).getCandidate();
		}
		throw new RuntimeException("missing profiler candidate for: " + token.toString() + " [" + token.getClass().getName() + "]");
	}
}
