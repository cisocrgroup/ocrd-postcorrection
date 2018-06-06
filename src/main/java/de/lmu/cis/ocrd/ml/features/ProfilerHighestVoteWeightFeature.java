package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.util.Optional;

public class ProfilerHighestVoteWeightFeature extends NamedDoubleFeature {
    private final Profile profile;

    public ProfilerHighestVoteWeightFeature(JsonObject o, ArgumentFactory args) throws Exception {
        this(JSONUtil.mustGetNameOrType(o), args);
    }

    private ProfilerHighestVoteWeightFeature(String name, ArgumentFactory args) throws Exception {
        super(name);
        this.profile = args.getProfile();
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }

    @Override
    protected double doCalculate(Token token, int i, int n) {
        final Optional<Candidates> candidates = profile.get(getWord(token, i, n).toString());
        return candidates.map(candidates1 -> candidates1.Candidates[0].Weight).orElse(0.0);
    }
}
