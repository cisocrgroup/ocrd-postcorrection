package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.Ranking;
import de.lmu.cis.ocrd.util.JSON;

import java.util.List;

public class DMDifferenceToNextRankFeature extends NamedDoubleFeature {

    private DMDifferenceToNextRankFeature(String name) {
        super(name);
    }

    public DMDifferenceToNextRankFeature(JsonObject o, ArgumentFactory args) {
        this(JSON.mustGetNameOrType(o));
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
        assert(handlesOCR(i, n));
		final List<Ranking> rs = token.getRankings();
		assert(!rs.isEmpty());
		final double first = rs.get(0).getRanking();
		double second = -1;
        if (rs.size() > 1) {
            second = rs.get(1).getRanking();
        }
        assert(first >= second);
        final double ret = Math.abs(first - second);
        assert(ret >= 0);
        return ret;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }
}
