package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateCaseClassFeature extends TokenCaseClassFeature {
	private static final long serialVersionUID = 6185953194478613281L;

	public CandidateCaseClassFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	public CandidateCaseClassFeature(String name) {
		super(name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public Object calculate(OCRToken token, int i, int n) {
		return getCaseClass(mustGetCandidate(token).Suggestion);
	}
}
