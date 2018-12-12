package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.util.JSON;
import de.lmu.cis.ocrd.ml.FreqMap;

public class MinCharNGramsFeature extends NamedCharacterNGramFeature {
	private static final long serialVersionUID = 1L;

	public MinCharNGramsFeature(JsonObject o, ArgumentFactory factory) throws Exception {
		this(JSON.mustGetNameOrType(o), factory.getCharacterTrigrams());
	}

	public MinCharNGramsFeature(String name, FreqMap ngrams) {
		super(name, ngrams);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		double min = Double.MAX_VALUE;
		final OCRWord word = getWord(token, i, n);
		for (String trigram : splitIntoCharacterNGrams(word.toString(), 3)) {
			final double val = getNgrams().getRelative(trigram);
			if (val < min) {
				min = val;
			}
		}
		return min;
	}
}
