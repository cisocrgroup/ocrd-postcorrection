package de.lmu.cis.ocrd.ml.features;

import java.util.List;
import java.util.Map;

public class DMDifferenceToNextRankFeature extends NamedDoubleFeature {
    private Map<OCRToken, List<Ranking>> rankings;
    public DMDifferenceToNextRankFeature(String name, Map<OCRToken, List<Ranking>> rankings) {
        super(name);
        this.rankings = rankings;
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
        assert(rankings.containsKey(token));
        List<Ranking> rs = rankings.get(token);
        assert(!rs.isEmpty());
        double before = Double.MAX_VALUE;
        for (Ranking r : rs) {
            assert(r.ranking < before);
        }
        double second = -1;
        if (rs.size() > 1) {
            second = rs.get(1).ranking;
        }
        return rs.get(0).ranking - second;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }
}
