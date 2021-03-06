package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

import java.util.ArrayList;
import java.util.List;

public class TokenLengthClassFeature extends NamedStringSetFeature {
	private static final long serialVersionUID = -1000888404407897300L;
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
		this(JSON.mustGetNameOrType(o), JSON.mustGet(o, "short").getAsInt(),
				JSON.mustGet(o, "medium").getAsInt(), JSON.mustGet(o, "long").getAsInt());
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
		final int tokenLength = token.getMasterOCR().getWordNormalized().length();
		return getLengthClass(tokenLength);
	}

	String getLengthClass(int tokenLength) {
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
