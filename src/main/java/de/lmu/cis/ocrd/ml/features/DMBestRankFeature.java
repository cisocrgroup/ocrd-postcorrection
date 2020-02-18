package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.Ranking;
import de.lmu.cis.ocrd.util.JSON;

import java.util.List;

public class DMBestRankFeature extends NamedDoubleFeature {
    private DMBestRankFeature(String name) {
        super(name);
    }

    public DMBestRankFeature(JsonObject o, ArgumentFactory args) {
        this(JSON.mustGetNameOrType(o));
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
		assert(handlesOCR(i, n));
		final List<Ranking> rs = token.getRankings();
		assert(!rs.isEmpty());

		// make sure that we have ordered rankings
        double before = Double.MAX_VALUE;
        for (Ranking r : rs) {
            assert(r.getRanking() < before);
        }
        return rs.get(0).getRanking();
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }
}
