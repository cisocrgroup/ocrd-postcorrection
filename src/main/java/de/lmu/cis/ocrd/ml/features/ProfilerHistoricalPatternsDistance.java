package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.util.JSON;

import java.util.List;

public class ProfilerHistoricalPatternsDistance extends NamedDoubleFeature {
	private static final long serialVersionUID = 2105792591421872162L;

	public ProfilerHistoricalPatternsDistance(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o));
	}

	private ProfilerHistoricalPatternsDistance(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final List<Candidate> cs = token.getAllProfilerCandidates();
		if (cs.isEmpty()) {
			return -1;
		}
		return cs.get(0).HistPatterns == null ? 0 :
				cs.get(0).HistPatterns.length;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
