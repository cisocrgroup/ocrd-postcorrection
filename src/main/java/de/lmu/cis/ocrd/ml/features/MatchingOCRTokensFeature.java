package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

public class MatchingOCRTokensFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 2004996085911141867L;

	public MatchingOCRTokensFeature(JsonObject o, ArgumentFactory factory) {
		this(JSON.mustGetNameOrType(o));
	}

	public MatchingOCRTokensFeature(String name) {
		super(name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyLastOtherOCR(i, n);
	}

	@Override
	public double doCalculate(OCRToken token, int i, int n) {
		assert (this.handlesOCR(i, n));
		double sum = 0;
		// i=0 is master OCR
		final String mOCR = token.getMasterOCR().getWordNormalized();
		for (int j = 1; j < n; j++) {
			if (mOCR.toLowerCase().equals(token.getSlaveOCR(j - 1).getWordNormalized().toLowerCase())) {
				sum += 1;
			}
		}
		return sum;
	}
}
