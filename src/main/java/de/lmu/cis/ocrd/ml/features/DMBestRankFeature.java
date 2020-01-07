package de.lmu.cis.ocrd.ml.features;

import java.util.List;

public class DMBestRankFeature extends NamedDoubleFeature {
    public DMBestRankFeature(String name) {
        super(name);
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
		assert(handlesOCR(i, n));
		final List<Ranking> rs = token.getRankings();
		assert(!rs.isEmpty());

		// make sure that we have ordered rankings
        double before = Double.MAX_VALUE;
        for (Ranking r : rs) {
            assert(r.ranking < before);
        }
        final double result =  rs.get(0).ranking;
        return result;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }
}
