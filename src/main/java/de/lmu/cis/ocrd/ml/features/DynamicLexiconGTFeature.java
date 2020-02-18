package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;

// DynamicLexiconGTFeature is a feature that simply checks if the master OCR of any given token
// equals its ground-truth. This feature should be used to simply add GT-data to
// the training and evaluation steps.
public class DynamicLexiconGTFeature extends NamedBooleanFeature {
	private static final long serialVersionUID = 1L;
	private static final int SHORT = 3; // setting of the profiler

	public DynamicLexiconGTFeature() {
		super("DynamicLexiconGT");
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	// Returns true if a OCR token should be taken into the dynamic lexicon for the
	// next profiler run. Any OCR token that is correct should be taken into the
	// profiler lexicon. If tokens are smaller than 4 letters, the profiler ignores
	// them, so small tokens should never be put into the dynamic lexicon.
	// Also if a token is either first or last in its line, the token should be ignored
	// since it could be a fragmented.
	@Override
	boolean doCalculate(OCRToken token, int i, int n) {
		final OCRWord mOCR = token.getMasterOCR();
		final String normalized = mOCR.getWordNormalized();
		return normalized.codePointCount(0, normalized.length()) > SHORT
				&& !mOCR.getLineNormalized().startsWith(normalized)
				&& !mOCR.getLineNormalized().endsWith(normalized)
				&& token.getGT().orElse("").equalsIgnoreCase(normalized);
	}
}
