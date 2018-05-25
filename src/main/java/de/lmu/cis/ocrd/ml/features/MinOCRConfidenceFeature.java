package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;

public class MinOCRConfidenceFeature extends NamedDoubleFeature {
	private final int ocrIndex;

	public MinOCRConfidenceFeature(JsonObject o, ArgumentFactory ignored) {
		this(JSONUtil.mustGetNameOrType(o), JSONUtil.mustGet(o, "ocrIndex").getAsInt());
	}

	public MinOCRConfidenceFeature(String name, int ocrIndex) {
		super(name);
		this.ocrIndex = ocrIndex;
	}

	@Override
	protected double doCalculate(Token token, int i, int n) {
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
		return handlesExactlyOCR(ocrIndex, i, n);
	}
}
