package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.Ranking;

import java.util.List;

public class DMDifferenceToNextRankFeature extends NamedDoubleFeature {
    public DMDifferenceToNextRankFeature(String name) {
        super(name);
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
        assert(handlesOCR(i, n));
		final List<Ranking> rs = token.getRankings();
		assert(!rs.isEmpty());
        double before = Double.MAX_VALUE;
		// make sure that we have ordered rankings
        for (Ranking r : rs) {
            assert(r.getRanking() < before);
        }
        double second = -1;
        if (rs.size() > 1) {
            second = rs.get(1).getRanking();
        }
        return rs.get(0).getRanking() - second;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }
}
