package de.lmu.cis.ocrd.ml.features;

import java.util.Optional;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.util.JSON;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

public class ProfilerHistoricalPatternsDistance extends NamedDoubleFeature {
	private static final long serialVersionUID = 2105792591421872162L;
	private final Profile profile;

	public ProfilerHistoricalPatternsDistance(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o), args.getProfile());
	}

	private ProfilerHistoricalPatternsDistance(String name, Profile profile) {
		super(name);
		this.profile = profile;
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final Optional<Candidates> candidates = profile.get(token.getMasterOCR().toString());
		if (!candidates.isPresent()) {
			return -1;
		}
		if (candidates.get().Candidates.length == 0) {
			return -1;
		}
		return candidates.get().Candidates[0].HistPatterns == null ? 0
				: candidates.get().Candidates[0].HistPatterns.length;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
