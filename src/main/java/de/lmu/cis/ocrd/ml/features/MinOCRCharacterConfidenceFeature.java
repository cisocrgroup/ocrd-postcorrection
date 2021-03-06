package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.util.JSON;

public class MinOCRCharacterConfidenceFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 5126850213064117487L;

	public MinOCRCharacterConfidenceFeature(JsonObject o, ArgumentFactory ignored) {
		this(JSON.mustGetNameOrType(o));
	}

	public MinOCRCharacterConfidenceFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final OCRWord word = getWord(token, i, n);
		double min = 1;
		for (int j = 0; j < word.getWordNormalized().length(); j++) {
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
