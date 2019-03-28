package de.lmu.cis.ocrd.ml.features;

import java.util.List;
import java.util.Map;

public class DMGTFeature extends NamedFeature {
	private final static String TRUE = Boolean.toString(true);
	private final static String DO_NOT_CARE = "do-not-care";
	private final static String FALSE = Boolean.toString(false);
	private Map<OCRToken, List<Ranking>> rankings;

	public DMGTFeature(String name, Map<OCRToken, List<Ranking>> rankings) {
		super(name);
		this.rankings = rankings;
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
		assert(rankings.containsKey(token));
		List<Ranking> rs = rankings.get(token);
		assert(!rs.isEmpty());

		final String gt = token.getGT().orElse("");
		// correct decision to use the top ranked candidate
		if (gt.equalsIgnoreCase(rs.get(0).candidate.Suggestion)) {
			return TRUE;
		}
		// the correction suggestion is false, but the ocr is correct: DO NOT "CORRECT" HERE.
		if (token.ocrIsCorrect()) {
			return FALSE;
		}
		// the correction suggestion is false; but so is the ocr token
		return DO_NOT_CARE;
	}
}
