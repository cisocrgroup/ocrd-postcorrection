package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;

import java.util.Random;

// Classifies OCR tokens into one of two different classes:
// * true        if the best ranked correction candidate equals the ground truth
//               token (the correction will improve or at least not harm the text accuracy)
// * false       if the best ranked correction candidate is wrong and the original
//               token was correct (the correction would disimprove the text accuracy)
// * do-not care if the best ranked candidate is false and the ocr token is also false
//               the correction will not improve nor disimprove the accuracy of the text.
public class RandomDMGTFeature extends DMGTFeature {

	private Random random;
	public RandomDMGTFeature(String name, long seed) {
		super(name);
		random = new Random(seed);
	}

	private boolean flip() {
		return random.nextBoolean();
	}

	// Returns true if the given token should be used for the DM-training.
	// In the do-not-care case, a true is returned in (approximately) 50% of the cases.
	@Override
	public boolean isValidForTraining(OCRToken token) {
		assert(!token.getRankings().isEmpty());
		final String gt = token.getGT().orElse("");
		if (!gt.toLowerCase().equals(token.getMasterOCR().getWordNormalized().toLowerCase())) {
			return flip();
		}
		return true;
	}
}
