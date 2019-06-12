package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.Prediction;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Workspace;
import de.lmu.cis.ocrd.profile.AdditionalLexicon;
import de.lmu.cis.ocrd.profile.AdditionalLexiconSet;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PostCorrectionCommand extends AbstractMLCommand {
    private static class Configuration extends AbstractMLCommand.Parameter {
        String lexiconExtensionProtocol = "";
        String decisionMakerProtocol = "";
        String additionalLexicon = "";
        boolean runLexiconExtension = false;
        boolean runDecisionMaker = false;
    }
    private Configuration config;
    private ModelZIP model;
    private Workspace workspace;
    private LM lm;

    @Override
    public String getName() {
        return "post-correct";
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        this.config = config.mustGetParameter(Configuration.class);
        setParameter(this.config); // setup super classes parameter for the profiler
        model = ModelZIP.open(Paths.get(this.config.model));
        workspace = new Workspace(Paths.get(config.mustGetMETSFile()));
        lm = new LM(false, Paths.get(this.config.trigrams));
        final String ifg = config.mustGetSingleInputFileGroup();
        final String ofg = config.mustGetSingleOutputFileGroup();
        AdditionalLexicon alex;
        if (this.config.runLexiconExtension) {
            alex = runDLE(ifg);
        } else {
            alex = openAlex();
        }
        if (this.config.runDecisionMaker) {
            final Map<OCRToken, List<Ranking>> rankings = runRR(ifg, alex);
            runDM(ifg, rankings, alex);
            saveOutputFileGroup(ofg);
        }
    }

    private void runDM(String ifg, Map<OCRToken, List<Ranking>> rankings, AdditionalLexicon alex) throws Exception {
        Logger.info("running decision maker step: {} ({})", ifg, config.nOCR);
        final Protocol protocol = new DecisionMakerProtocol();
        final List<OCRToken> tokens = readTokens(workspace.getMETS(), ifg, alex);
        lm.setTokens(tokens);
        final FeatureSet fs = new FeatureSet()
                .add(new DMBestRankFeature("dm-best-rank", rankings))
                .add(new DMDifferenceToNextRankFeature("dm-difference-to-next", rankings));
        final LogisticClassifier c = LogisticClassifier.load(model.openDMModel(config.nOCR-1));
        for (OCRToken token : tokens) {
            if (!rankings.containsKey(token)) {
                continue;
            }
            final Prediction p = c.predict(fs.calculateFeatureVector(token, config.nOCR));
            final boolean prediction = p.getPrediction();
            protocol.register(token, prediction);
            if (prediction) {
                final Ranking ranking = rankings.get(token).get(0);
                final String correction = ranking.candidate.getAsSuggestionFor(token.getMasterOCR().getWord());
                Logger.info("correcting '{}' with '{}' ({})",
                        token.getMasterOCR().toString(), correction, ranking.ranking);
                token.correct(correction, ranking.ranking);
            }
        }
        saveProtocol(protocol, config.decisionMakerProtocol);
    }

    private Map<OCRToken, List<Ranking>> runRR(String ifg, AdditionalLexicon alex) throws Exception {
        Logger.info("running ranking step: {} ({})", ifg, config.nOCR);
        final List<OCRToken> tokens = readTokens(workspace.getMETS(), ifg, alex);
        lm.setTokens(tokens);
        final FeatureSet fs = makeFeatureSet(model.openRRFeatureSet());
        final LogisticClassifier c = LogisticClassifier.load(model.openRRModel(config.nOCR-1));
        Map<OCRToken, List<Ranking>> rankings = new HashMap<>();
        for (OCRToken token : tokens) {
            for (Candidate candidate : token.getAllProfilerCandidates()) {
                if (!rankings.containsKey(token)) {
                    rankings.put(token, new ArrayList<>());
                }
                final FeatureSet.Vector values = fs.calculateFeatureVector(new OCRTokenWithCandidateImpl(token, candidate), config.nOCR);
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

    private AdditionalLexicon runDLE(String ifg) throws Exception {
        Logger.info("running lexicon extension step: {} ({})", ifg, config.nOCR);
        final Protocol protocol = new LexiconExtensionProtocol();
        final List<OCRToken> tokens = readTokens(workspace.getMETS(), ifg, new NoAdditionalLexicon());
        lm.setTokens(tokens);
        final FeatureSet fs = makeFeatureSet(model.openDLEFeatureSet());
        final LogisticClassifier c = LogisticClassifier.load(model.openDLEModel(config.nOCR-1));
        AdditionalLexiconSet alex = new AdditionalLexiconSet();
        for (OCRToken token: tokens) {
            if (token.isLexiconEntry()) {
                Logger.debug("skipping lexicon entry: {}", token.toString());
                continue;
            }
            final FeatureSet.Vector values = fs.calculateFeatureVector(token, config.nOCR-1);
            final boolean prediction = c.predict(values).getPrediction();
            protocol.register(token, prediction);
            if (prediction) {
                Logger.debug("adding to extended lexicon: {}", token.getMasterOCR().toString());
                alex.add(token.getMasterOCR().toString());
            }
        }
        saveProtocol(protocol, config.lexiconExtensionProtocol);
        return alex;
    }

    private static class AlexTokens {
        List<String> tokens;
    }

    private AdditionalLexicon openAlex() throws IOException {
        final Path p = Paths.get(config.additionalLexicon);
        try(Reader r = new InputStreamReader(new FileInputStream(p.toFile()), StandardCharsets.UTF_8)) {
            AlexTokens alexTokens = new Gson().fromJson(r, AlexTokens.class);
            AdditionalLexiconSet newAlex = new AdditionalLexiconSet();
            for (String token : alexTokens.tokens) {
                newAlex.add(token);
            }
            return newAlex;
        }
    }

    private void saveProtocol(Protocol protocol, String path) throws IOException {
        if (path == null || "".equals(path)) {
            return;
        }
        final Path p = Paths.get(path);
        try(Writer w = new OutputStreamWriter(new FileOutputStream(p.toFile()), StandardCharsets.UTF_8)) {
            w.write(protocol.toJSON());
            w.flush();
        }
    }

    private void saveOutputFileGroup(String ofg) throws Exception {
        for (Page page : getPages()) {
            workspace.putPageXML(page, ofg);
        }
    }

    private FeatureSet makeFeatureSet(InputStream is) throws Exception {
        try (InputStream iis = is) {
            final String json = IOUtils.toString(iis, Charset.forName("UTF-8"));
            final JsonObject[] os = new Gson().fromJson(json, JsonObject[].class);
            return FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(os, getFeatureClassFilter());
        }
    }

}
