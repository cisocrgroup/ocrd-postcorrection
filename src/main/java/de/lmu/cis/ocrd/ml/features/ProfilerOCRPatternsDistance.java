package de.lmu.cis.ocrd.ml.features;

import java.util.Optional;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.util.JSON;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

public class ProfilerOCRPatternsDistance extends NamedDoubleFeature {
	private static final long serialVersionUID = -8093738603831136066L;
	private final Profile profile;

	public ProfilerOCRPatternsDistance(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSON.mustGetNameOrType(o), args.getProfile());
	}

	private ProfilerOCRPatternsDistance(String name, Profile profile) {
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
		return candidates.get().Candidates[0].Distance;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
