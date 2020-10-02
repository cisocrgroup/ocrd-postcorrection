package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.util.JSON;

import java.util.List;

public class HighestRankedCandidateMatchesOCRFeature extends NamedBooleanFeature {
	private static final long serialVersionUID = 2905792591421472862L;

	public HighestRankedCandidateMatchesOCRFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	public HighestRankedCandidateMatchesOCRFeature(String name) {
		super(name);
	}

	@Override
	protected boolean doCalculate(OCRToken token, int i, int n) {
		final List<Candidate> cs = token.getCandidates();
		if (cs.isEmpty()) {
			return false;
		}
		return cs.get(0).Suggestion.toLowerCase().equals(getWord(token, i, n).getWordNormalized().toLowerCase());
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}
}
