package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.BinaryPrediction;
import de.lmu.cis.ocrd.profile.Candidate;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Rankings extends HashMap<OCRToken, List<Ranking>> {
    // sort the rankings in descending order from highest rank to lowest rank.
    private void sort(List<Ranking> rs) {
        assert(!rs.isEmpty());
        rs.sort((lhs, rhs)-> Double.compare(rhs.getRanking(), lhs.getRanking()));
        double before = rs.get(0).getRanking();
        // ensure descending order of the list
        for (int i = 1; i < rs.size(); i++) {
            final double current = rs.get(i).getRanking();
            assert (before >= current);
        }
    }

    // sort all rankings in this map.
    public void sort() {
        for (Entry<OCRToken, List<Ranking>> entry: this.entrySet()) {
            sort(entry.getValue());
        }
    }

    public static Rankings load(OCRTokenReader tokenReader, Path rrModel, Path rrTrain) throws Exception {
        final LogisticClassifier classifier = LogisticClassifier.load(rrModel);
        final Instances instances = new ConverterUtils.DataSource(rrTrain.toString()).getDataSet();
        instances.setClassIndex(instances.numAttributes() - 1);

        final Iterator<Instance> iis = instances.iterator();
        final Iterator<OCRToken> tis = TokenFilter.filter(tokenReader.read()).collect(Collectors.toList()).iterator();
        final Rankings rankings = new Rankings();
        while (iis.hasNext() && tis.hasNext()) {
            final Instance instance = iis.next();
            final OCRToken token = tis.next();

            // calculate a ranking for each of the token's candidates and put it into the map
            List<Ranking> rs = null;
            for (Candidate candidate: token.getCandidates()) {
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
        rankings.sort();
        return rankings;
    }
}
