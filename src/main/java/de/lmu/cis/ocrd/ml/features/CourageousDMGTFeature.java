package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;

// Classifies OCR tokens into one of two different classes:
// * true        if the best ranked correction candidate equals the ground truth
//               token (the correction will improve or at least not harm the text accuracy)
// * false       if the best ranked correction candidate is wrong and the original
//               token was correct (the correction would disimprove the text accuracy)
// * do-not care if the best ranked candidate is false and the ocr token is also false
//               the correction will not improve nor disimprove the accuracy of the text.
public class CourageousDMGTFeature extends DMGTFeature {

	public CourageousDMGTFeature(String name) {
		super(name);
	}

	// Returns true if the given token should be used for the DM-training.
	// This is not the case if a wrong w_ocr has wrong correction suggestion (in this case
	// it does not matter if the w_ocr is corrected or not).
	@Override
	public boolean isValidForTraining(OCRToken token) {
		assert(!token.getRankings().isEmpty());
		final String gt = token.getGT().orElse("").toLowerCase();
		if (!gt.toLowerCase().equals(token.getMasterOCR().getWordNormalized())) {
			return token.getRankings().get(0).getCandidate().Suggestion.toLowerCase().equals(gt);
		}
		return true;
	}
}
