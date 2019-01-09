package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.util.JSON;

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
		final double[] values = getNgrams().getRelativeNGrams(str, 3);
		if (values == null || values.length == 0) {
			return 0;
		}
		double max = values[0];
		for (int i = 1; i < values.length; i++) {
			max = Double.max(max, values[i]);
		}
		return max;
	}
}
