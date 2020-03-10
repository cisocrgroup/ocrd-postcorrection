package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.profile.PosPattern;

abstract class AbstractHistoricalPatternConfidenceFeature extends AbstractConfidenceFeature {
	AbstractHistoricalPatternConfidenceFeature(String name) {
		super(name);
	}

	double[] getPatternConfidence(OCRWord word, PosPattern pattern) {
		final int[] ocrToken = word.getWordNormalized().codePoints().toArray();
		final int pos = pattern.getAdjustedPosition(ocrToken);
		final int[] right = pattern.Right.codePoints().toArray();
		if (pos == -1 || right.length == 0) {
			return new double[]{0};
		}
		return doGetPatternConfidence(word, pos, right);
	}

	private double[] doGetPatternConfidence(OCRWord word, int pos, int[] right) {
		final double[] values = new double[right.length];
		for (int i = 0; i < right.length; i++) {
			values[i] = word.getCharacterConfidenceAt(i + pos);
		}
		return values;
	}
}
