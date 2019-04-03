package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.PosPattern;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateMinHistoricalPatternConfidenceFeature extends AbstractHistoricalPatternConfidenceFeature {
	private static final long serialVersionUID = -3415753568125497094L;

	public CandidateMinHistoricalPatternConfidenceFeature(JsonObject o,
	                                                      ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	public CandidateMinHistoricalPatternConfidenceFeature(String name) {
		super(name);
	}

	@Override
	protected double getConfidence(OCRToken token, int i, int n) {
		final OCRWord word = getWord(token, i, n);
		final Candidate candidate = mustGetCandidate(token);
		double min = 1;
		for (PosPattern p: candidate.OCRPatterns) {
			final double[] confidences = getPatternConfidence(word, p);
			for (double confidence : confidences) {
				min = Double.min(min, confidence);
			}
		}
		return min;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
