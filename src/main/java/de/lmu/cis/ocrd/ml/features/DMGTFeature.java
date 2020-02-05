package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.Ranking;

// Classifies OCR tokens into one of two different classes:
// * true        if the best ranked correction candidate equals the ground truth
//               token (the correction will improve or at least not harm the text accuracy)
// * false       if the best ranked correction candidate is wrong and the original
//               token was correct (the correction would disimprove the text accuracy)
// * do-not care if the best ranked candidate is false and the ocr token is also false
//               the correction will not improve nor disimprove the accuracy of the text.
public class DMGTFeature extends NamedBooleanFeature {
	public DMGTFeature(String name) {
		super(name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	boolean doCalculate(OCRToken token, int i, int n) {
		assert(handlesOCR(i, n));
		assert(!token.getRankings().isEmpty());
		final Ranking ranking = token.getRankings().get(0);

		final String gt = token.getGT().orElse("");
		// correct decision to use the top ranked candidate
		return gt.equalsIgnoreCase(ranking.getCandidate().Suggestion);
//		// the correction suggestion is false, but the ocr is correct: DO NOT "CORRECT" HERE.
//		if (gt.equalsIgnoreCase(token.getMasterOCR().toString())) {
//			return false;
//		}
//		// the correction suggestion is false; but so is the ocr token
//		return DO_NOT_CARE;
	}

	// Returns true if the given token should be used for the DM-training.
	// This is not the case if a wrong w_ocr has wrong correction suggestion (in this case
	// it does not matter if the w_ocr is corrected or not).
	public static boolean isValidForTraining(OCRToken token) {
		assert(!token.getRankings().isEmpty());
		if (!token.getGT().orElse("").equalsIgnoreCase(token.getMasterOCR().toString())) {
			return token.getRankings().get(0).getCandidate().Suggestion.equalsIgnoreCase(token.getGT().orElse(""));
		}
		return true;
	}
}
