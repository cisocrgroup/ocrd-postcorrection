package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

public class TokenLengthFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 6518045049131388557L;

	public TokenLengthFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	@SuppressWarnings("WeakerAccess")
	protected TokenLengthFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		return getWord(token, i, n).getWordNormalized().length();
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
