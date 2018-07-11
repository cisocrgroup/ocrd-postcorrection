package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;

public class TokenLengthFeature extends NamedDoubleFeature {
	public TokenLengthFeature(JsonObject o, ArgumentFactory args) {
		this(JSONUtil.mustGetNameOrType(o));
	}

	protected TokenLengthFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(Token token, int i, int n) {
		return (double) getWord(token, i, n).toString().length();
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
