package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.PosPattern;
import de.lmu.cis.ocrd.util.JSON;

public class CandidateHistoricalPatternsDistanceFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 2105792591421672162L;

	public CandidateHistoricalPatternsDistanceFeature(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o));
	}

	private CandidateHistoricalPatternsDistanceFeature(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final PosPattern[] patterns = mustGetCandidate(token).HistPatterns;
		return patterns == null ? 0 : patterns.length;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
