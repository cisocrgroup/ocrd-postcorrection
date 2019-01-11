package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.PosPattern;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateMaxHistoricalPatternConfidenceFeature extends AbstractHistoricalPatternConfidenceFeature {
	private static final long serialVersionUID = -3415753568125497094L;

	public CandidateMaxHistoricalPatternConfidenceFeature(JsonObject o,
	                                                      ArgumentFactory args) {
		this(JSON.mustGetNameOrType(o));
	}

	public CandidateMaxHistoricalPatternConfidenceFeature(String name) {
		super(name);
	}

	@Override
	public double doCalculate(OCRToken token, int i, int n) {
		final OCRWord word = getWord(token, i, n);
		final Candidate candidate = mustGetCandidate(token);
		double max = Double.MIN_VALUE;
		for (PosPattern p: candidate.OCRPatterns) {
			final double[] confidences = getPatternConfidence(word, p);
			for (double confidence : confidences) {
				max = Double.max(max, confidence);
			}
		}
		return max;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
