package de.lmu.cis.ocrd.ml.features;

import org.pmw.tinylog.Logger;

import java.util.List;
import java.util.Map;

public class DMBestRankFeature extends NamedDoubleFeature {
    private Map<OCRToken, List<Ranking>> rankings;
    public DMBestRankFeature(String name, Map<OCRToken, List<Ranking>> rankings) {
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
        final double result =  rs.get(0).ranking;
        Logger.debug("{} result = {}", getName(), result);
        return result;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }
}
