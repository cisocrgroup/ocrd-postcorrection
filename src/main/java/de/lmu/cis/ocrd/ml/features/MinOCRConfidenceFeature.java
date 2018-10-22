package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.json.JSONUtil;

public class MinOCRConfidenceFeature extends NamedDoubleFeature {

	public MinOCRConfidenceFeature(JsonObject o, ArgumentFactory ignored) {
		this(JSONUtil.mustGetNameOrType(o));
	}

	public MinOCRConfidenceFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final Word word = getWord(token, i, n);
		double min = Double.MAX_VALUE;
		for (int j = 0; j < word.getSize(); j++) {
			final double confidence = word.getConfidenceAt(j);
			if (confidence < min) {
				min = confidence;
			}
		}
		return min;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
