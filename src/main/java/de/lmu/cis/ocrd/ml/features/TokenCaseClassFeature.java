package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

import java.util.ArrayList;
import java.util.List;

public class TokenCaseClassFeature extends NamedStringSetFeature {
	private static final long serialVersionUID = 6185953194478613291L;
	private static final String LOWER = "all-lower-case";
	private static final String UPPER = "all-upper-case";
	private static final String TITLE = "title-case";
	private static final String MIXED = "mixed-case";
	private static final List<String> CLASSES = new ArrayList<>();

	static {
		CLASSES.add(LOWER);
		CLASSES.add(UPPER);
		CLASSES.add(TITLE);
		CLASSES.add(MIXED);
	}

	public TokenCaseClassFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	public TokenCaseClassFeature(String name) {
		super(name, CLASSES);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		return getCaseClass(getWord(token, i, n).toString());
	}

	protected String getCaseClass(String str) {
		boolean allLowerCase = true;
		boolean allUpperCase = true;
		boolean firstUpperCase = false;
		boolean first = true;
		for (int c : str.codePoints().toArray()) {
			final int type = Character.getType(c);
			if (type == Character.UPPERCASE_LETTER) {
				firstUpperCase = first;
				allLowerCase = false;
			} else if (type == Character.LOWERCASE_LETTER) {
				allUpperCase = false;
			} else {
				allUpperCase = false;
				allLowerCase = false;
			}
			first = false;
		}

		if (allLowerCase) {
			return LOWER;
		}
		if (allUpperCase) {
			return UPPER;
		}
		if (firstUpperCase) {
			return TITLE;
		}
		return MIXED;
	}
}
