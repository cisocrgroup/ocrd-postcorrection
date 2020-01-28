package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluateCommand extends ParametersCommand {
    private Counts counts;

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
        try(Writer w = new FileWriter(parameters.getDMTraining().getEvaluation(parameters.getNOCR()).toFile())) {
            new Gson().toJson(counts, w);
        }
    }

    private void evaluate(String ifg) throws Exception {
        final Profile profile = getProfile(ifg, new NoAdditionalLexicon(), parameters.getNOCR());
        final DMProtocol protocol = new DMProtocol();
        try (InputStream is = new FileInputStream(parameters.getDMTraining().getProtocol(parameters.getNOCR()).toFile())) {
            protocol.read(is);
        }
        workspace.getBaseOCRTokenReader(ifg).read().forEach(t -> evaluate(profile, protocol, t));
    }

    private void evaluate(Profile profile, DMProtocol protocol, de.lmu.cis.ocrd.ml.BaseOCRToken token) {
        final String gt = token.getGT().orElse("").toLowerCase();
        if (gt.isEmpty()) {
            return;
        }
        if (protocol.getProtocol().corrections.containsKey(token.getMasterOCR().getID())) {
            final DMProtocol.ProtocolValue value = protocol.getProtocol().corrections.get(token.getMasterOCR().getID());
            evaluate(value, getCandidates(profile, value.ocr), gt);
        } else {
            final String mOCR = token.getMasterOCR().getWordNormalized().toLowerCase();
            evaluate(mOCR, getCandidates(profile, mOCR), gt);
        }
    }

    private void evaluate(DMProtocol.ProtocolValue value, List<Candidate> candidates, String gt) {
        counts.n++;
        if (value.ocr.equalsIgnoreCase(gt)) {
            counts.correctBefore++;
        }
        if (isTypeIError(value.ocr, candidates, gt)) {
            counts.typeI++;
            counts.typeIValues.add(value);
        } else if (isTypeIIError(value, candidates, gt)) {
            counts.typeII++;
            counts.typeIIValues.add(value);
        } else if (isTypeIIIError(value, gt)) {
            counts.typeIII++;
            counts.typeIIIValues.add(value);
        } else if (isTypeIVError(value, gt)) {
            counts.typeIV++;
            counts.typeIVValues.add(value);
        }
        if ((value.taken && value.cor.equalsIgnoreCase(gt)) ||
                (!value.taken && value.ocr.equalsIgnoreCase(gt))) {
            counts.correctAfter++;
        }
        if (value.taken && value.cor.equalsIgnoreCase(gt) && !value.ocr.equalsIgnoreCase(gt)) {
            counts.successfulCorrections++;
        }
    }

    private void evaluate(String mOCR, List<Candidate> candidates, String gt) {
        counts.n++;
        if (mOCR.equalsIgnoreCase(gt)) {
            counts.correctBefore++;
        }
        if (isTypeIError(mOCR, candidates, gt)) {
            counts.typeI++;
        }
    }

    // No good correction candidate
    private static boolean isTypeIError(String mOCR, List<Candidate> candidates, String gt) {
        if (mOCR.equalsIgnoreCase(gt)) {
            return false;
        }
        return !hasGoodCandidate(candidates, gt);
    }

    // Good correction not on first rank
    private static boolean isTypeIIError(DMProtocol.ProtocolValue value, List<Candidate> candidates, String gt) {
        if (value.ocr.equalsIgnoreCase(gt) || value.cor.equalsIgnoreCase(gt)) {
            return false;
        }
        return hasGoodCandidate(candidates, gt);
    }

    // Missed opportunity
    private static boolean isTypeIIIError(DMProtocol.ProtocolValue value, String gt) {
        if (value.ocr.equalsIgnoreCase(gt)) {
            return false;
        }
        return value.cor.equalsIgnoreCase(gt) && !value.taken;
    }

    // Infelicitous correction
    private static boolean isTypeIVError(DMProtocol.ProtocolValue value, String gt) {
        if (value.ocr.equalsIgnoreCase(gt)) {
            return false;
        }
        return !value.cor.equalsIgnoreCase(gt) && value.taken;
    }

    private static boolean hasGoodCandidate(List<Candidate> candidates, String gt) {
        if (candidates == null) {
            return false;
        }
        for (Candidate candidate: candidates) {
            if (candidate.Suggestion.equalsIgnoreCase(gt)) {
                return true;
            }
        }
        return false;
    }

    private List<Candidate> getCandidates(Profile profile, String mOCR) {
        Optional<Candidates> candidate = profile.get(mOCR);
        return candidate.map(candidates -> candidates.Candidates.subList(0, Math.min(candidates.Candidates.size(), parameters.getMaxCandidates()))).orElse(null);
    }

    public static class Counts {
        List<DMProtocol.ProtocolValue> typeIValues = new ArrayList<>();
        List<DMProtocol.ProtocolValue> typeIIValues = new ArrayList<>();
        List<DMProtocol.ProtocolValue> typeIIIValues = new ArrayList<>();
        List<DMProtocol.ProtocolValue> typeIVValues = new ArrayList<>();
        int typeI = 0;   // no correction candidate
        int typeII = 0;  // good correction not on rank one
        int typeIII = 0; // missed opportunity
        int typeIV = 0;  // infelicitous correction
        int correctBefore = 0;
        int correctAfter = 0;
        int successfulCorrections = 0;
        int n = 0;
    }
}
