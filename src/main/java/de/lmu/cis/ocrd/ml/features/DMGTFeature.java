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
		return gt.equalsIgnoreCase(ranking.getCandidate().Suggestion);
	}

	// Returns true if the given token should be used for the DM-training.
	public boolean isValidForTraining(OCRToken token) {
		return true;
	}
}
