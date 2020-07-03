package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.util.JSON;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class CandidateOCRPatternsDistanceFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = -8093738673831136066L;
	private final LevenshteinDistance ld;

	public CandidateOCRPatternsDistanceFeature(JsonObject o, ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o), JSON.mustGet(o, "maxThreshold").getAsInt());
	}

	private CandidateOCRPatternsDistanceFeature(String name, int maxThreshold) {
		super(name);
		this.ld = new LevenshteinDistance(maxThreshold);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final Candidate candidate = mustGetCandidate(token);
		if (i == 0) {
			return candidate.Distance;
		}
		final String ocr = getWord(token, i, n).getWordNormalized();
		final String suggestion = candidate.Suggestion;
		return ld.apply(ocr, suggestion);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}
}
