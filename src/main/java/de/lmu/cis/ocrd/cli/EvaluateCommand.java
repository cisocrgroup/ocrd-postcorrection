package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.BaseOCRToken;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.ml.Model;
import de.lmu.cis.ocrd.profile.AdditionalFileLexicon;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ocr-tokens
// ├ not suspicious
// │ ├ too short
// │ ├ no candidates
// │ └ lexicon-entry
// │   └ false friends
// └ suspicious
//   ├ replaced
//   │ ├ ocr correct
//   │ │ ├ candidate correct
//   │ │ └ candidate incorrect [infelicitous correction (type IV)]
//   │ │   ├ no correction candidate (type I)
//   │ │   └ correction candidate not on first rank (type II)
//   │ └ ocr incorrect
//   │   ├ candidate correct [successful correction]
//   │	 └ candidate incorrect [do not care correction]
//   │     ├ no correction candidate (type I)
//   │     └ correction candidate not on first rank (type II)
//   └ not replaced
//     ├ ocr correct
//     │ ├ candidate correct
//     │ └ candidate incorrect
//     │   ├ no correction candidate (type I)
//     │   └ correction candidate not on first rank (type II)
//     └ ocr incorrect
//       ├ candidate correct [missed opportunity (type III)]
//   	 └ candidate incorrect
//         ├ no correction candidate (type I)
//         └ correction candidate not on first rank (type II)
public class EvaluateCommand extends PostCorrectionCommand {
    private Counts counts;
    private DMProtocol protocol;
    private Profile profile;

    public EvaluateCommand() {
        super("eval");
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        super.execute(config); // run post correction
        config.setCommand(this); // reset log name
        final String[] ifgs = config.mustGetInputFileGroups();
        if (config.isIterate()) {
            iterate((nOCR, runLE)->evaluate(ifgs, nOCR, runLE));
        } else {
            evaluate(ifgs, parameters.getNOCR(), parameters.isRunLE());
        }
    }

    private void evaluate(String[] ifgs, int nOCR, boolean runLE) throws Exception {
        this.counts = new Counts();
        for (String ifg : ifgs) {
            Logger.debug("evaluate({}, {}, {})", ifg, nOCR, runLE);
            if (runLE) {
                profile = getProfile(ifg, new AdditionalFileLexicon(parameters.getLETraining().getLexicon(nOCR)), nOCR);
            } else {
                profile = getProfile(ifg, new NoAdditionalLexicon(), nOCR);
            }
            protocol = new DMProtocol();
            Logger.debug("reading protocol to {}", parameters.getDMTraining().getProtocol(nOCR, runLE).toString());
            try (InputStream is = new FileInputStream(parameters.getDMTraining().getProtocol(nOCR, runLE).toFile())) {
                protocol.read(is);
            }
            for (BaseOCRToken token : workspace.getBaseOCRTokenReader(ifg).read()) {
                evaluate(token);
            }
        }
        countDecisionMakerTrainingTokens(nOCR);
        writeCounts(nOCR, runLE);
    }

    private void writeCounts(int nOCR, boolean runLE) throws IOException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        counts.timestamp = timestamp.getTime();
        counts.time = timestamp.toString();
        final Path path = parameters.getDMTraining().getEvaluation(nOCR, runLE);
        Logger.debug("writeCounts({}, {}) to file", nOCR, runLE, path);
        try (Writer w = new FileWriter(path.toFile())) {
            new Gson().toJson(counts, w);
        }
    }

    private void evaluate(BaseOCRToken token) throws Exception {
        final String gt = token.getGT().orElse("").toLowerCase();
        if (gt.isEmpty()) {
            return;
        }
        // counts
        counts.n++;
        if (token.getMasterOCR().getWordNormalized().toLowerCase().equals(gt)) {
            counts.correctBefore++;
        }
        Logger.debug("evaluating token {}/{} {}", counts.n, counts.correctBefore, token.toString());
        // corrections
        final String id = token.getID();
        if (protocol.getProtocol().corrections.containsKey(id)) {
            final DMProtocol.Value value = protocol.getProtocol().corrections.get(id);
            if (!value.gt.present) {
                throw new Exception("invalid protocol value " + id + ": missing ground truth");
            }
            countSuspicious(value);
        } else {
            countSkipped(token, gt);
        }
    }

    private void countSuspicious(DMProtocol.Value value) {
        counts.suspicious++;
        if (value.taken) {
            countSuspiciousReplaced(value);
        } else {
            countSuspiciousNotReplaced(value);
        }
        if (value.ocrIsCorrect() && !value.correctionIsCorrect()) {
            counts.risks++;
        }
        if (!value.ocrIsCorrect() && value.correctionIsCorrect()) {
            counts.chances++;
        }
        if ((value.taken && value.correctionIsCorrect()) || (!value.taken && value.ocrIsCorrect())) {
            counts.correctAfter++;
        }
    }

    private void countSuspiciousReplaced(DMProtocol.Value value) {
        assert(value.taken);
        counts.replaced++;
        if (value.ocrIsCorrect()) {
            if (!value.correctionIsCorrect()) {
                Logger.debug(" * type IV error (infelicitous correction)");
                counts.typeIVValues.add(value);
                counts.infelicitousCorrection++;
                if (value.getCorrectCandidateIndex() == -1) {
                    Logger.debug(" * type I error (missing candidate)");
                    counts.typeIValues.add(value);
                    counts.missingCandidate++;
                } else {
                    Logger.debug(" * type II error (bad rank)");
                    counts.typeIIValues.add(value);
                    counts.badRank++;
                }
            }
        } else {
            if (value.correctionIsCorrect()) {
                counts.successfulCorrections++;
            } else {
                if (value.getCorrectCandidateIndex() == -1) {
                    Logger.debug(" * type I error (missing candidate)");
                    counts.typeIValues.add(value);
                    counts.missingCandidate++;
                } else {
                    Logger.debug(" * type II error (bad rank)");
                    counts.typeIIValues.add(value);
                    counts.badRank++;
                }
            }
        }
    }

    private void countSuspiciousNotReplaced(DMProtocol.Value value) {
        counts.notReplaced++;
        assert (!value.taken);
        if (value.ocrIsCorrect()) {
            if (!value.correctionIsCorrect()) {
                if (value.getCorrectCandidateIndex() == -1) {
                    Logger.debug(" * type I error (missing candidate)");
                    counts.typeIValues.add(value);
                    counts.missingCandidate++;
                } else {
                    Logger.debug(" * type II error (bad rank)");
                    counts.typeIIValues.add(value);
                    counts.badRank++;
                }
            }
        } else {
            if (value.correctionIsCorrect()) {
                Logger.debug(" * type III error (missed opportunity)");
                counts.typeIIIValues.add(value);
                counts.missedOpportunity++;
            } else {
                if (value.getCorrectCandidateIndex() == -1) {
                    Logger.debug(" * type I error (missing candidate)");
                    counts.typeIValues.add(value);
                    counts.missingCandidate++;
                } else {
                    Logger.debug(" * type II error (bad rank)");
                    counts.typeIIValues.add(value);
                    counts.badRank++;
                }
            }
        }
    }

    private void countSkipped(BaseOCRToken token, String gt) throws Exception {
        counts.skipped++;
        if (token.getMasterOCR().getWordNormalized().length() <= 3) {
            counts.skippedTooShort++;
            Logger.debug(" * no correction attempted: too short");
        } else {
            final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
            if (!maybeCandidates.isPresent() || maybeCandidates.get().Candidates.isEmpty()) {
                counts.skippedNoCandidates++;
                Logger.debug(" * no correction attempted: skipped because no candidates");
            } else if (maybeCandidates.get().Candidates.size() == 1 && maybeCandidates.get().Candidates.get(0).isLexiconEntry()) {
                counts.skippedLexiconEntry++;
                Logger.debug(" * no correction attempted: lexicon entry");
                if (!token.getMasterOCR().getWordNormalized().toLowerCase().equals(gt)) {
                    counts.skippedFalseFriends++;
                    Logger.debug(" * no correction attempted: false friend");
                }
            } else {
                throw new Exception("bad unhandled token: " + token.toString());
            }
        }
        if (token.getMasterOCR().getWordNormalized().toLowerCase().equals(token.getGT().orElse("").toLowerCase())) {
            counts.correctAfter++;
        }
    }

    private void countDecisionMakerTrainingTokens(int nOCR) throws Exception {
        final Model model = Model.open(parameters.getModel());
        final int index = model.getDMFeatureSet().size();
        final Path path = parameters.getDMTraining().getTraining(nOCR);
        try (InputStream is = new FileInputStream(path.toFile())) {
            final ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(is);
            final Instances instances = dataSource.getDataSet();
            final Instances structure = dataSource.getStructure();
            instances.setClassIndex(structure.numAttributes() -1);
            counts.dmTotalTrainingInstances = instances.size();
            for (final Instance instance : instances) {
                final Attribute attribute = instance.attribute(structure.numAttributes() - 1);
                if (instance.classValue() == 0) { // 0 is the true class
                    counts.dmTrueTrainingInstances++;
                }
            }
        }
        Logger.debug("total number of dm training instances: {}", counts.dmTotalTrainingInstances);
        Logger.debug("total number of true dm training instances: {}", counts.dmTrueTrainingInstances);
    }

    public static class Counts {
        List<DMProtocol.Value> typeIValues = new ArrayList<>();
        List<DMProtocol.Value> typeIIValues = new ArrayList<>();
        List<DMProtocol.Value> typeIIIValues = new ArrayList<>();
        List<DMProtocol.Value> typeIVValues = new ArrayList<>();
        String time;
        long timestamp;
        int dmTrueTrainingInstances = 0;
        int dmTotalTrainingInstances = 0;
        int missingCandidate = 0;         // no correction candidate (error type I)
        int badRank = 0;                  // good correction not on rank one (error type II)
        int missedOpportunity = 0;        // missed opportunity (error type III)
        int infelicitousCorrection = 0;   // infelicitous correction (error type IV)
        int correctBefore = 0;
        int correctAfter = 0;
        int replaced = 0;                 // decision maker said yes
        int notReplaced = 0;              // decision make said no
        int successfulCorrections = 0;    // a wrong OCR token was corrected with a correct correction
        int chances = 0;
        int risks = 0;
        int suspicious = 0;               // number of tokens where a correction was attempted
        int skipped = 0;                  // no correction attempt because one of the three causes below
        int skippedTooShort = 0;          // no correction attempt because the token is too short
        int skippedNoCandidates = 0;      // no correction attempt because the token has no correction candidates
        int skippedLexiconEntry = 0;      // no correction attempt because the token is a lexicon entry
        int skippedFalseFriends = 0;      // token is a lexicon entry but it is an OCR error
        int n = 0;                        // total number of tokens
    }
}
