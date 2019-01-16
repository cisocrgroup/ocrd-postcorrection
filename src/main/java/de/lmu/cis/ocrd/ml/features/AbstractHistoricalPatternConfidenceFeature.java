package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.profile.PosPattern;

abstract class AbstractHistoricalPatternConfidenceFeature extends NamedDoubleFeature {
	AbstractHistoricalPatternConfidenceFeature(String name) {
		super(name);
	}

	double[] getPatternConfidence(OCRWord word,
	                                            PosPattern pattern) {
		final int[] cps = pattern.Right.codePoints().toArray();
		if (cps.length == 0) {
			return doGetPatternConfidence(word, pattern.Pos,
					pattern.Left.codePoints().toArray());
		}
		return doGetPatternConfidence(word, pattern.Pos, cps);
	}

	private double[] doGetPatternConfidence(OCRWord word, int pos, int[] cps) {
		final double[] values = new double[cps.length];
		for (int i = 0; i < cps.length; i++) {
			values[i] = word.getCharacterConfidenceAt(i + pos);
		}
		return values.length == 0 ? new double[]{0.0} : values;
	}
}