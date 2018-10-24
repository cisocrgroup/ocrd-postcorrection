package de.lmu.cis.ocrd.ml.features;

abstract class BaseFeatureImplementation implements Feature {
	protected static boolean handlesOnlyMasterOCR(int i, int ignored) {
		return i == 0;
	}

	static boolean handlesOnlyLastOtherOCR(int i, int n) {
		return (i + 1) == n && i > 0;
	}

	protected static boolean handlesEveryOtherOCR(int i, int n) {
		return !handlesOnlyMasterOCR(i, n);
	}

	static boolean handlesExactlyOCR(int ocr, int i, int n) {
		return ocr == i;
	}

	static boolean handlesAnyOCR(int i, int n) {
		return true;
	}

	final Word getWord(OCRToken token, int i, int n) {
		assert (i >= 0);
		assert (handlesOCR(i, n));
		if (i == 0) { // master OCR
			return token.getMasterOCR();
		}
		return token.getOtherOCR(i - 1);
	}
}
