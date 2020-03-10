package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.BinaryPrediction;
import de.lmu.cis.ocrd.profile.Candidate;
import org.pmw.tinylog.Logger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Rankings extends HashMap<OCRToken, List<Ranking>> {
    // compare rankings; use the ranking weight and the candidate weight as fallback
    private static int compare(Ranking lhs, Ranking rhs) {
        if (lhs.getRanking() == rhs.getRanking()) {
            return Double.compare(lhs.getCandidate().Weight, rhs.getCandidate().Weight);
        }
        return Double.compare(lhs.getRanking(), rhs.getRanking());
    }

    // sort the rankings in descending order from highest rank to lowest rank.
    private void sort(List<Ranking> rs) {
        assert(!rs.isEmpty());
        rs.sort((lhs, rhs)-> compare(rhs, lhs));
        double before = rs.get(0).getRanking();
        // ensure descending order of the list
        for (int i = 1; i < rs.size(); i++) {
            final double current = rs.get(i).getRanking();
            assert (before >= current);
        }
    }

    // sort all rankings in this map.
    public void sortAllRankings() {
        for (Entry<OCRToken, List<Ranking>> entry: this.entrySet()) {
            sort(entry.getValue());
        }
    }

    public static Rankings load(OCRTokenReader tokenReader, Path rrModel, Path rrTrain) throws Exception {
        final LogisticClassifier classifier = LogisticClassifier.load(rrModel);
        final Instances instances = new ConverterUtils.DataSource(rrTrain.toString()).getDataSet();
        instances.setClassIndex(instances.numAttributes() - 1);

        int i = 0;
        final Iterator<Instance> iis = instances.iterator();
        // final Iterator<OCRToken> tis = TokenFilter.filter(tokenReader.read()).collect(Collectors.toList()).iterator();
        final Rankings rankings = new Rankings();
        for (OCRToken token: TokenFilter.filter(tokenReader.read(), (t)->t.getGT().isPresent()).collect(Collectors.toList())) {
            // calculate a ranking for each of the token's candidates and put it into the map
            List<Ranking> rs = null;
            for (Candidate candidate: token.getCandidates()) {
                i++;
                if (!iis.hasNext()) {
                    Logger.debug("i = {}", i);
                    throw new Exception("tokens and instances out of sync");
                }
                final Instance instance = iis.next();
                final BinaryPrediction p = classifier.predict(instance);
                final boolean t = p.getPrediction();
                final double ranking = t ? p.getConfidence() : -p.getConfidence();
                if (!((t && ranking >= 0) || (!t && ranking <= 0))) {
                    throw new Exception("bad ranking: " + t + "," + ranking);
                }

                if (Double.isNaN(ranking)) {
                    continue;
                }
                if (rs == null) {
                    rs = new ArrayList<>();
                }
                rs.add(new Ranking(candidate, ranking));
            }
            // put into map
            assert(!rankings.containsKey(token));
            if (rs != null) {
                rankings.put(token, rs);
            }
        }
        rankings.sortAllRankings();
        return rankings;
    }
}
