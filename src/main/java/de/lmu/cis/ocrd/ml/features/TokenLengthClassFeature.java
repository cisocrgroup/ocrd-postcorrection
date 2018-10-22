package de.lmu.cis.ocrd.ml.features;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.json.JSONUtil;

public class TokenLengthClassFeature extends NamedStringSetFeature {
	private final static String SHORT = "short-token";
	private final static String MEDIUM = "medium-token";
	private final static String LONG = "long-token";
	private final static String VERY_LONG = "very-long-token";

	private static final List<String> CLASSES = new ArrayList<>();

	static {
		CLASSES.add(SHORT);
		CLASSES.add(MEDIUM);
		CLASSES.add(LONG);
		CLASSES.add(VERY_LONG);
	}

	private final int shrt, medium, lng;

	public TokenLengthClassFeature(JsonObject o, ArgumentFactory args) {
		this(JSONUtil.mustGetNameOrType(o), JSONUtil.mustGet(o, "short").getAsInt(),
				JSONUtil.mustGet(o, "medium").getAsInt(), JSONUtil.mustGet(o, "long").getAsInt());
	}

	public TokenLengthClassFeature(String name, int shrt, int medium, int lng) {
		super(name, CLASSES);
		this.shrt = shrt;
		this.medium = medium;
		this.lng = lng;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		final int tokenLength = token.getMasterOCR().toString().length();
		if (tokenLength <= shrt) {
			return SHORT;
		}
		if (tokenLength <= medium) {
			return MEDIUM;
		}
		if (tokenLength <= lng) {
			return LONG;
		}
		return VERY_LONG;
	}
}
