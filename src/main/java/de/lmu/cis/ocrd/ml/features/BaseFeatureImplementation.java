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

	protected static boolean handlesEverySlaveOCR(int i, int n) {
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

	// return either the candidate of the token or the best ranked candidate for the token
	protected Candidate mustGetCandidate(OCRToken token) {
		final Optional<Candidate> candidate = token.getCandidate();
		if (candidate.isPresent()) {
			return candidate.get();
		}
		List<Ranking> rankings = token.getRankings();
		if (rankings.isEmpty()) {
			throw new RuntimeException("missing profiler candidate for: " + token.toString() + " [" + token.getClass().getName() + "]");
		}
		return rankings.get(0).getCandidate();
	}
}
