package de.lmu.cis.ocrd.cli;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Workspace;
import de.lmu.cis.ocrd.profile.AdditionalLexicon;
import de.lmu.cis.ocrd.profile.AdditionalLexiconSet;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import org.pmw.tinylog.Logger;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class PostCorrectionCommand extends AbstractMLCommand {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    private ModelZIP model;
    private Workspace workspace;
    private LM lm;

    @Override
    public String getName() {
        return "post-correct";
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        config.setCommand(this);
        setParameter(config);
        model = ModelZIP.open(Paths.get(getParameter().model));
        workspace = new Workspace(Paths.get(config.mustGetMETSFile()));
        try (InputStream is = model.openLanguageModel()) {
            lm = new LM(is);
        }
        final String ifg = config.mustGetSingleInputFileGroup();
        final String ofg = config.mustGetSingleOutputFileGroup();
        AdditionalLexicon alex = new NoAdditionalLexicon();
        Logger.info("command: {}, input file group: {}", getName(), ifg);
        Logger.info("model {} created at: {}", getParameter().model, sdf.format(new Date(model.getCreated())));
        Logger.debug("loaded {} language model trigrams", lm.getCharacterTrigrams().getTotal());
        if (getParameter().runLE) {
            alex = runLE(ifg);
        }
        if (getParameter().runDM) {
            final Map<OCRToken, List<Ranking>> rankings = runRR(ifg, alex);
            runDM(ifg, rankings, alex);
            saveOutputFileGroup(ofg);
        }
    }

    private void runDM(String ifg, Map<OCRToken, List<Ranking>> rankings, AdditionalLexicon alex) throws Exception {
        Logger.info("running decision maker step: {} ({})", ifg, getParameter().nOCR);
        final Protocol protocol = new DMProtocol();
        final List<OCRToken> tokens = readOCRTokens(workspace.getMETS(), ifg, alex);
        Logger.debug("read {} OCR tokens from input file group {}", tokens.size(), ifg);
        lm.setTokens(tokens);
        final FeatureSet fs = new FeatureSet()
                .add(new DMBestRankFeature("dm-best-rank", rankings))
                .add(new DMDifferenceToNextRankFeature("dm-difference-to-next", rankings));
        final LogisticClassifier c = LogisticClassifier.load(model.openDMModel(getParameter().nOCR-1));
        for (OCRToken token : tokens) {
            if (!rankings.containsKey(token)) {
                continue;
            }
            final Prediction p = c.predict(fs.calculateFeatureVector(token, getParameter().nOCR));
            final boolean prediction = p.getPrediction();
            final Ranking ranking = rankings.get(token).get(0);
            final String correction = ranking.candidate.getAsSuggestionFor(token.getMasterOCR().getWordNormalized());
            protocol.protocol(token, correction, p.getConfidence(), prediction);
            if (prediction) {
                Logger.debug("correcting '{}' with '{}' ({})",
                        token.getMasterOCR().toString(), correction, ranking.ranking);
                token.correct(correction, ranking.ranking);
            }
        }
        saveProtocol(protocol, getParameter().dmTraining.protocol);
    }

    private Map<OCRToken, List<Ranking>> runRR(String ifg, AdditionalLexicon alex) throws Exception {
        Logger.info("running ranking step: {} ({})", ifg, getParameter().nOCR);
        final List<OCRToken> tokens = readOCRTokens(workspace.getMETS(), ifg, alex);
        Logger.debug("read {} OCR tokens from input file group {}", tokens.size(), ifg);
        lm.setTokens(tokens);
        final FeatureSet fs = makeFeatureSet(model.getRRFeatureSet());
        final LogisticClassifier c = LogisticClassifier.load(model.openRRModel(getParameter().nOCR-1));
        Map<OCRToken, List<Ranking>> rankings = new HashMap<>();
        for (OCRToken token : tokens) {
            for (Candidate candidate : token.getAllProfilerCandidates()) {
                if (!rankings.containsKey(token)) {
                    rankings.put(token, new ArrayList<>());
                }
                final FeatureSet.Vector values = fs.calculateFeatureVector(new OCRTokenWithCandidateImpl(token, candidate), getParameter().nOCR);
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

    private AdditionalLexicon runLE(String ifg) throws Exception {
        Logger.info("running lexicon extension step: {} ({})", ifg, getParameter().nOCR);
        final Protocol protocol = new LEProtocol();
        final List<OCRToken> tokens = readOCRTokens(workspace.getMETS(), ifg, new NoAdditionalLexicon());
        Logger.debug("read {} OCR tokens from input file group {}", tokens.size(), ifg);
        lm.setTokens(tokens);
        final FeatureSet fs = makeFeatureSet(model.getLEFeatureSet());
        final LogisticClassifier c = LogisticClassifier.load(model.openLEModel(getParameter().nOCR-1));
        AdditionalLexiconSet alex = new AdditionalLexiconSet();
        for (OCRToken token: tokens) {
            if (token.isLexiconEntry()) {
                Logger.debug("skipping lexicon entry: {}", token.toString());
                continue;
            }
            final FeatureSet.Vector values = fs.calculateFeatureVector(token, getParameter().nOCR); //  -1);
            final Prediction p = c.predict(values);
            final boolean prediction = p.getPrediction();
            protocol.protocol(token, "", p.getConfidence(), prediction);
            if (prediction) {
                Logger.debug("adding to extended lexicon: {}", token.getMasterOCR().toString());
                alex.add(token.getMasterOCR().toString());
            }
        }
        saveProtocol(protocol, getParameter().leTraining.protocol);
        return alex;
    }

    private void saveProtocol(Protocol protocol, String path) throws Exception {
        if (path == null || "".equals(path)) {
            return;
        }
        final Path p = Paths.get(path);
        Logger.debug("saving protocol to {}", p.toString());
        try(OutputStream out = new FileOutputStream(p.toFile())) {
            protocol.write(out);
            out.flush();
        }
    }

    private void saveOutputFileGroup(String ofg) throws Exception {
        for (Page page : getPages()) {
            workspace.putPageXML(page, ofg);
        }
    }

    private FeatureSet makeFeatureSet(List<JsonObject> set) throws Exception {
        return FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(set, getFeatureClassFilter());
    }

}
