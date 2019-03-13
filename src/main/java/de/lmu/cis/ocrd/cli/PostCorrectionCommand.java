package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.Prediction;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.profile.AdditionalLexicon;
import de.lmu.cis.ocrd.profile.AdditionalLexiconSet;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

public class PostCorrectionCommand extends AbstractMLCommand {
    private ModelZIP model;
    private METS mets;
    private LM lm;

    @Override
    public String getName() {
        return "post-correct";
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        setParameter(config);
        final Parameter parameter = getParameter();
        model = ModelZIP.open(Paths.get(parameter.model));
        mets = METS.open(Paths.get(config.mustGetMETSFile()));
        lm = new LM(false, Paths.get(getParameter().trigrams));
        final String ifg = config.mustGetSingleInputFileGroup();
        final String ofg = config.mustGetSingleOutputFileGroup();
        final AdditionalLexicon alex = runDLE(ifg, parameter.nOCR);
        final Map<OCRToken, List<Ranking>> rankings = runRR(ifg, alex, parameter.nOCR);
        runDM(ifg, rankings, alex, parameter.nOCR);
    }

    private void runDM(String ifg, Map<OCRToken, List<Ranking>> rankings, AdditionalLexicon alex, int nOCR) throws Exception {
        final List<OCRToken> tokens = readTokens(mets, ifg, alex);
        lm.setTokens(tokens);
        final FeatureSet fs = new FeatureSet()
                .add(new DMBestRankFeature("dm-best-rank", rankings))
                .add(new DMDifferenceToNextRankFeature("dm-difference-to-next", rankings));
        final LogisticClassifier c = LogisticClassifier.load(model.openDMModel(nOCR-1));
        for (OCRToken token : tokens) {
            if (!rankings.containsKey(token)) {
                continue;
            }
            final Prediction p = c.predict(fs.calculateFeatureVector(token, nOCR));
            if (p.getPrediction()) {
                final Ranking ranking = rankings.get(token).get(0);
                token.correct(ranking.candidate.Suggestion, ranking.ranking);
            }
        }
    }

    private Map<OCRToken, List<Ranking>> runRR(String ifg, AdditionalLexicon alex, int nOCR) throws Exception {
        final List<OCRToken> tokens = readTokens(mets, ifg, alex);
        lm.setTokens(tokens);
        final FeatureSet fs = makeFeatureSet(model.openRRFeatureSet());
        final LogisticClassifier c = LogisticClassifier.load(model.openRRModel(nOCR-1));
        Map<OCRToken, List<Ranking>> rankings = new HashMap<>();
        for (OCRToken token : tokens) {
            for (Candidate candidate : token.getAllProfilerCandidates()) {
                if (!rankings.containsKey(token)) {
                    rankings.put(token, new ArrayList<>());
                }
                final FeatureSet.Vector values = fs.calculateFeatureVector(new OCRTokenWithCandidateImpl(token, candidate), nOCR);
                final Prediction p = c.predict(values);
                final double ranking = p.getPrediction()? p.getConfidence() : -p.getConfidence();
                rankings.get(token).add(new Ranking(candidate, ranking));
            }
            if (rankings.containsKey(token)) {
                rankings.get(token).sort(Comparator.comparingDouble(lhs -> lhs.ranking));
            }
        }
        return rankings;
    }

    private AdditionalLexicon runDLE(String ifg, int nOCR) throws Exception {
        final List<OCRToken> tokens = readTokens(mets, ifg, new NoAdditionalLexicon());
        lm.setTokens(tokens);
        final FeatureSet fs = makeFeatureSet(model.openDLEFeatureSet());
        final LogisticClassifier c = LogisticClassifier.load(model.openDLEModel(nOCR-1));
        AdditionalLexiconSet alex = new AdditionalLexiconSet();
        for (OCRToken token: tokens) {
           final FeatureSet.Vector values = fs.calculateFeatureVector(token, nOCR-1);
            if (c.predict(values).getPrediction()) {
                alex.add(token.getMasterOCR().toString());
            }
        }
        return alex;
    }

    private FeatureSet makeFeatureSet(InputStream is) throws Exception {
        try (InputStream iis = is) {
            final String json = IOUtils.toString(iis, Charset.forName("UTF-8"));
            final JsonObject[] os = new Gson().fromJson(json, JsonObject[].class);
            return FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(os);
        }
    }

}
