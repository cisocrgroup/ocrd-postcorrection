package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.util.JSON;

public class MaxOCRCharacterConfidenceFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 1424827046143811274L;

	public MaxOCRCharacterConfidenceFeature(JsonObject o, ArgumentFactory ignored) {
		this(JSON.mustGetNameOrType(o));
	}

	public MaxOCRCharacterConfidenceFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final OCRWord word = getWord(token, i, n);
		double max = 0;
		for (int j = 0; j < word.getWord().length(); j++) {
			final double confidence = word.getCharacterConfidenceAt(j);
			max = Double.max(max, confidence);
		}
		return max;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}
}
