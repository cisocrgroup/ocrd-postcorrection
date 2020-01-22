package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.Ranking;

import java.util.List;

// Classifies OCR tokens into one of two different classes:
// * true        if the best ranked correction candidate equals the ground truth
//               token (the correction will improve or at least not harm the text accuracy)
// * false       if the best ranked correction candidate is wrong and the original
//               token was correct (the correction would disimprove the text accuracy)
// * do-not care if the best ranked candidate is false and the ocr token is also false
//               the correction will not improve nor disimprove the accuracy of the text.
public class DMGTFeature extends NamedFeature {
	private final static String TRUE = Boolean.toString(true);
	private final static String DO_NOT_CARE = "do-not-care";
	private final static String FALSE = Boolean.toString(false);

	public DMGTFeature(String name) {
		super(name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public final String getClasses() {
		// the correct classification index is zero as for boolean features;
		// any other index (1, or 2) means not to correct.
		return String.format("{%s,%s,%s}", TRUE, DO_NOT_CARE, FALSE);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		assert(handlesOCR(i, n));
		final List<Ranking> rs = token.getRankings();
		assert(!rs.isEmpty());

		final String gt = token.getGT().orElse("");
		// correct decision to use the top ranked candidate
		if (gt.equalsIgnoreCase(rs.get(0).getCandidate().Suggestion)) {
			return TRUE;
		}
		// the correction suggestion is false, but the ocr is correct: DO NOT "CORRECT" HERE.
		if (token.getGT().orElse("").equalsIgnoreCase(token.getMasterOCR().toString())) {
			return FALSE;
		}
		// the correction suggestion is false; but so is the ocr token
		return DO_NOT_CARE;
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
