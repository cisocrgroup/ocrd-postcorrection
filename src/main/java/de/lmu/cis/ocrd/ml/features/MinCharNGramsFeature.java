package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.util.JSON;

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
		return getMinCharNGram(getWord(token, i, n).toString());
	}

	protected double getMinCharNGram(String str) {
		final double[] values = getNgrams().getRelativeNGrams(str, 3);
		if (values == null || values.length == 0) {
			return 0;
		}
		double min = values[0];
		for (int i = 1; i < values.length; i++) {
			min = Double.min(min, values[i]);
		}
		return min;
	}
}
