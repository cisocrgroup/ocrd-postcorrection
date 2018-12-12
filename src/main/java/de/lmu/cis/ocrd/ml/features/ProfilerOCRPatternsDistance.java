package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.util.JSON;

import java.util.List;

public class ProfilerOCRPatternsDistance extends NamedDoubleFeature {
	private static final long serialVersionUID = -8093738603831136066L;

	public ProfilerOCRPatternsDistance(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o));
	}

	private ProfilerOCRPatternsDistance(String name) {
		super(name);
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final List<Candidate> cs = token.getAllProfilerCandidates();
		if (cs.isEmpty()) {
			return -1;
		}
		return cs.get(0).Distance;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
