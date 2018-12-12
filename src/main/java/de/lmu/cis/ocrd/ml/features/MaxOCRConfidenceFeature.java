package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.util.JSON;

public class MaxOCRConfidenceFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 1424827046143811274L;

	public MaxOCRConfidenceFeature(JsonObject o, ArgumentFactory ignored) {
		this(JSON.mustGetNameOrType(o));
	}

	public MaxOCRConfidenceFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final OCRWord word = getWord(token, i, n);
		double max = 0;
		for (int j = 0; j < word.getWord().length(); j++) {
			final double confidence = word.getConfidenceAt(j);
			if (confidence > max) {
				max = confidence;
			}
		}
		return max;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
