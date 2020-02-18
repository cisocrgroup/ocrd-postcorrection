package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.BaseOCRToken;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.ml.Ranking;
import de.lmu.cis.ocrd.profile.AdditionalFileLexicon;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.util.Normalizer;
import org.pmw.tinylog.Logger;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluateCommand extends ParametersCommand {
    private Counts counts;
    private DMProtocol protocol;
    private Profile profile;

    public EvaluateCommand() {
        super("eval");
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        init(config);
        config.setCommand(this); // logging
        this.counts = new Counts();
        for (String ifg: config.mustGetInputFileGroups()) {
            evaluate(ifg);
        }
        try(Writer w = new FileWriter(parameters.getDMTraining().getEvaluation(parameters.getNOCR(), parameters.isRunLE()).toFile())) {
            new Gson().toJson(counts, w);
        }
    }

    private void evaluate(String ifg) throws Exception {
        if (parameters.isRunLE()) {
            profile = getProfile(ifg, new AdditionalFileLexicon(parameters.getLETraining().getLexicon(parameters.getNOCR())), parameters.getNOCR());
        } else {
            profile = getProfile(ifg, new NoAdditionalLexicon(), parameters.getNOCR());
        }
        protocol = new DMProtocol();
        Logger.debug("reading protocol to {}", parameters.getDMTraining().getProtocol(parameters.getNOCR(), parameters.isRunLE()).toString());
        try (InputStream is = new FileInputStream(parameters.getDMTraining().getProtocol(parameters.getNOCR(), parameters.isRunLE()).toFile())) {
            protocol.read(is);
        }
        for (BaseOCRToken token: workspace.getBaseOCRTokenReader(ifg).read()) {
            evaluate(token);
        }
    }

    private void evaluate(BaseOCRToken token) throws Exception {
        final String gt = token.getGT().orElse("").toLowerCase();
        if (gt.isEmpty()) {
            return;
        }
        // counts
        counts.n++;
        if (token.getMasterOCR().getWordNormalized().equalsIgnoreCase(gt)) {
            counts.correctBefore++;
        }
        // corrections
        final String id = token.getID();
        if (protocol.getProtocol().corrections.containsKey(id)) {
            final DMProtocol.Value value = protocol.getProtocol().corrections.get(id);
            if (!value.gt.present) {
                throw new Exception("invalid protocol value " + id + ": missing ground truth");
            }
            countAttempted(value);
        } else {
            countNotAttempted(token, gt);
        }
    }

    private void countAttempted(DMProtocol.Value value) {
        counts.attempted++;
        if (isTypeIError(value)) {
            counts.missingCandidate++;
            counts.typeIValues.add(value);
        }
        if (isTypeIIError(value)) {
            counts.badRank++;
            counts.typeIIValues.add(value);
        }
        if (isTypeIIIError(value)) {
            counts.missedOpportunity++;
            counts.typeIIIValues.add(value);
        }
        if (isTypeIVError(value)) {
            counts.infelicitousCorrection++;
            counts.typeIVValues.add(value);
        }
        if ((value.taken && correctionIsGood(value)) || (!value.taken && !correctionIsGood(value))) {
            counts.correctAfter++;
        }
        if (value.taken && correctionIsGood(value)) {
            counts.successfulCorrections++;
        }
    }

    private void countNotAttempted(BaseOCRToken token, String gt) throws Exception {
        counts.skipped++;
        if (token.getMasterOCR().getWordNormalized().length() <= 3) {
            counts.skippedTooShort++;
        } else {
            final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
            if (!maybeCandidates.isPresent() || maybeCandidates.get().Candidates.isEmpty()) {
                counts.skippedNoCandidates++;
            } else if (maybeCandidates.get().Candidates.size() == 1 && maybeCandidates.get().Candidates.get(0).isLexiconEntry()) {
                counts.skippedLexiconEntry++;
                if (!token.getMasterOCR().getWordNormalized().equalsIgnoreCase(gt)) {
                    counts.skippedFalseFriends++;
                }
            } else {
                throw new Exception("bad unhandled token: " + token.toString());
            }
        }
        if (token.getMasterOCR().getWordNormalized().equalsIgnoreCase(token.getGT().orElse(""))) {
            counts.correctAfter++;
        }
    }

    // No good correction candidate
    private static boolean isTypeIError(DMProtocol.Value value) {
        if (value.taken && !correctionIsGood(value)) {
            return !hasGoodCandidate(value);
        }
        return false;
    }

    // Good correction not on first rank
    private static boolean isTypeIIError(DMProtocol.Value value) {
        if (value.taken && !correctionIsGood(value)) {
            return hasGoodCandidate(value);
        }
        return false;
    }

    // Missed opportunity
    private static boolean isTypeIIIError(DMProtocol.Value value) {
        return !value.taken && correctionIsGood(value);
    }

    // Infelicitous correction
    private static boolean isTypeIVError(DMProtocol.Value value) {
        return value.taken && !correctionIsGood(value);
    }

    private static boolean correctionIsGood(DMProtocol.Value value) {
        return Normalizer.normalize(value.cor).equalsIgnoreCase(value.gt.gt);
    }

    private static boolean hasGoodCandidate(DMProtocol.Value value) {
        if (value.rankings == null) {
            return false;
        }
        // no need to normalize: gt.gt is already normalized; profiler's suggestions are as well normalized.
        for (Ranking ranking: value.rankings) {
            if (ranking.getCandidate().Suggestion.equalsIgnoreCase(value.gt.gt)) {
                return true;
            }
        }
        return false;
    }

    public static class Counts {
        List<DMProtocol.Value> typeIValues = new ArrayList<>();
        List<DMProtocol.Value> typeIIValues = new ArrayList<>();
        List<DMProtocol.Value> typeIIIValues = new ArrayList<>();
        List<DMProtocol.Value> typeIVValues = new ArrayList<>();
        int missingCandidate = 0;        // no correction candidate (error type I)
        int badRank = 0;                 // good correction not on rank one (error type II)
        int missedOpportunity = 0;       // missed opportunity (error type III)
        int infelicitousCorrection = 0;  // infelicitous correction (error type IV)
        int correctBefore = 0;
        int correctAfter = 0;
        int successfulCorrections = 0;   // a wrong OCR token was corrected with a correct correction
        int attempted = 0;               // number of tokens where a correction was
        int skipped = 0;                 // no correction attempt because one of the three causes below
        int skippedTooShort = 0;         // no correction attempt because the token is too short
        int skippedNoCandidates = 0;     // no correction attempt because the token has no correction candidates
        int skippedLexiconEntry = 0;     // no correction attempt because the token is a lexicon entry
        int skippedFalseFriends = 0;     // token is a lexicon entry but it is an OCR error
        int n = 0;                       // total number of tokens
    }
}
