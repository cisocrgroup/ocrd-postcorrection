package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.util.Optional;

public class ProfilerOCRPatternsDistance extends NamedDoubleFeature {
	private final Profile profile;

	public ProfilerOCRPatternsDistance(JsonObject o, ArgumentFactory args) throws Exception {
		this(JSONUtil.mustGetNameOrType(o), args.getProfile());
	}

	private ProfilerOCRPatternsDistance(String name, Profile profile) {
		super(name);
		this.profile = profile;
	}

	@Override
	protected double doCalculate(Token token, int i, int n) {
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
