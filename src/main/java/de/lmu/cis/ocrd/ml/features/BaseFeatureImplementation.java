package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.profile.Candidate;

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

	protected Candidate mustGetCandidate(OCRToken token) {
		final Optional<Candidate> candidate = token.getProfilerCandidate();
		if (!candidate.isPresent()) {
			throw new RuntimeException("missing profiler candidate for: " + token.toString() + " [" + token.getClass().getName() + "]");
		}
		return candidate.get();
	}
}
