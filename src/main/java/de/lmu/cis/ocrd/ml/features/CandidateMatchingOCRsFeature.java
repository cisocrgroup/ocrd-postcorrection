package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateMatchingOCRsFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = -2004996085911141867L;

	public CandidateMatchingOCRsFeature(JsonObject o, ArgumentFactory factory) {
		this(JSON.mustGetNameOrType(o));
	}

	public CandidateMatchingOCRsFeature(String name) {
		super(name);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	public double doCalculate(OCRToken token, int i, int n) {
		int sum = 0;
		final String suggestion = mustGetCandidate(token).Suggestion;
		if (token.getMasterOCR().toString().equals(suggestion)) {
			sum += 1;
		}
		for (int j = 0; j < token.getNOCR()-1; j++) {
			if (token.getSlaveOCR(j).toString().equals(suggestion)) {
				sum += 1;
			}
		}
		return sum;
	}
}
