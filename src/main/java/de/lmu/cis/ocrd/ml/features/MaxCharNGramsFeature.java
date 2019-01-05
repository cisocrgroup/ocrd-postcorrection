package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.util.JSON;
import de.lmu.cis.ocrd.ml.FreqMap;

public class MaxCharNGramsFeature extends NamedCharacterNGramFeature {
	private static final long serialVersionUID = 1L;

	public MaxCharNGramsFeature(JsonObject o, ArgumentFactory factory) throws Exception {
		this(JSON.mustGetNameOrType(o), factory.getCharacterTrigrams());
	}

	public MaxCharNGramsFeature(String name, FreqMap ngrams) {
		super(name, ngrams);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		return getMaxCharNGram(getWord(token, i, n).toString());
	}

	protected double getMaxCharNGram(String str) {
		double max = Double.MIN_VALUE;
		for (String trigram : splitIntoCharacterNGrams(str, 3)) {
			final double val = getNgrams().getRelative(trigram);
			max = Double.max(max, val);
		}
		return max;
	}
}
