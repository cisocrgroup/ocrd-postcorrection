package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

public class OCRWordConfidenceFeature extends NamedDoubleFeature {

	public OCRWordConfidenceFeature(JsonObject o, ArgumentFactory ignored) {
		this(JSON.mustGetNameOrType(o));
	}

	protected OCRWordConfidenceFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		return getWord(token, i, n).getConfidence();
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}
}
