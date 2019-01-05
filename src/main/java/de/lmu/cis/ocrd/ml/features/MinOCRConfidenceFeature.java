package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.util.JSON;

public class MinOCRConfidenceFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 5126850213064117487L;

	public MinOCRConfidenceFeature(JsonObject o, ArgumentFactory ignored) {
		this(JSON.mustGetNameOrType(o));
	}

	public MinOCRConfidenceFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final OCRWord word = getWord(token, i, n);
		double min = Double.MAX_VALUE;
		for (int j = 0; j < word.getWord().length(); j++) {
			final double confidence = word.getCharacterConfidenceAt(j);
			min = Double.min(min, confidence);
		}
		return min;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}
}
