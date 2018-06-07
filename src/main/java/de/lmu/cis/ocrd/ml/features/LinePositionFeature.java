package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;

import java.util.Arrays;

public class LinePositionFeature extends NamedStringSetFeature {
	private static final String FIRST = "first-in-line";
	private static final String MIDDLE = "in-line";
	private static final String LAST = "last-in-line";

	public LinePositionFeature(JsonObject o, ArgumentFactory args) {
		this(JSONUtil.mustGetNameOrType(o));
	}

	LinePositionFeature(String name) {
		super(name, Arrays.asList(new String[]{FIRST, MIDDLE, LAST}));
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public Object calculate(Token token, int i, int n) {
		if (token.getID() == 1) {
			return FIRST;
		}
		// TODO: return LAST
		return MIDDLE;
	}
}
