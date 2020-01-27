package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.BinaryPrediction;
import de.lmu.cis.ocrd.profile.Candidate;
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
    public static Rankings load(OCRTokenReader tokenReader, Path rrModel, Path rrTrain) throws Exception {
        final LogisticClassifier classifier = LogisticClassifier.load(rrModel);
        final Instances instances = new ConverterUtils.DataSource(rrTrain.toString()).getDataSet();
        instances.setClassIndex(instances.numAttributes() - 1);

        final Iterator<Instance> iis = instances.iterator();
        final Iterator<OCRToken> tis = TokenFilter.filter(tokenReader.read()).collect(Collectors.toList()).iterator();
        final Rankings rankings = new Rankings();
        while (iis.hasNext() && tis.hasNext()) {
            final Instance i = iis.next();
            final OCRToken t = tis.next();
            for (Candidate candidate: t.getCandidates()) {
                final BinaryPrediction p = classifier.predict(i);
                final double ranking = p.getPrediction() ? p.getConfidence() : -p.getConfidence();
                if (Double.isNaN(ranking)) {
                    continue;
                }
                if (!rankings.containsKey(t)) {
                    rankings.put(t, new ArrayList<>());
                }
                rankings.get(t).add(new Ranking(candidate, ranking));
            }
            if (rankings.containsKey(t)) {
                rankings.get(t).sort((lhs, rhs) -> Double.compare(rhs.getRanking(), lhs.getRanking()));
            }
        }
        return rankings;
    }
}
