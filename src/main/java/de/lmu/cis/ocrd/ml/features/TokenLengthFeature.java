package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.json.JSONUtil;

public class TokenLengthFeature extends NamedDoubleFeature {
	public TokenLengthFeature(JsonObject o, ArgumentFactory args) {
		this(JSONUtil.mustGetNameOrType(o));
	}

	protected TokenLengthFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		return getWord(token, i, n).toString().length();
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
