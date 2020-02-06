package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.BaseOCRToken;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.ml.Ranking;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class EvaluateCommand extends ParametersCommand {
    private Counts counts;
    private DMProtocol protocol;

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
        protocol = new DMProtocol();
        try (InputStream is = new FileInputStream(parameters.getDMTraining().getProtocol(parameters.getNOCR()).toFile())) {
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
            attempted(value);
        } else {
            notAttempted(token);
        }
    }

    private void attempted(DMProtocol.Value value) {
        if (isTypeIError(value)) {
            counts.typeI++;
            counts.typeIValues.add(value);
        } else if (isTypeIIError(value)) {
            counts.typeII++;
            counts.typeIIValues.add(value);
        } else if (isTypeIIIError(value)) {
            counts.typeIII++;
            counts.typeIIIValues.add(value);
        } else if (isTypeIVError(value)) {
            counts.typeIV++;
            counts.typeIVValues.add(value);
        }
        if ((value.taken && value.cor.equalsIgnoreCase(value.gt.gt)) ||
                (!value.taken && value.ocr.equalsIgnoreCase(value.gt.gt))) {
            counts.correctAfter++;
        }
        // ok                                                but this V ??
        if (value.taken && value.cor.equalsIgnoreCase(value.gt.gt) && !value.ocr.equalsIgnoreCase(value.gt.gt)) {
            counts.successfulCorrections++;
        }
    }

    private void notAttempted(BaseOCRToken token) {
        counts.skipped++;
        if (token.getMasterOCR().getWordNormalized().length() <= 3) {
            counts.tooShort++;
        }
    }

    // No good correction candidate
    private static boolean isTypeIError(DMProtocol.Value value) {
        if (value.taken && !value.cor.equalsIgnoreCase(value.gt.gt)) {
            return !hasGoodCandidate(value);
        }
        return false;
    }

    // Good correction not on first rank
    private static boolean isTypeIIError(DMProtocol.Value value) {
        if (value.taken && !value.cor.equalsIgnoreCase(value.gt.gt)) {
            return hasGoodCandidate(value);
        }
        return false;
    }

    // Missed opportunity
    private static boolean isTypeIIIError(DMProtocol.Value value) {
        return !value.taken && value.cor.equalsIgnoreCase(value.gt.gt);
    }

    // Infelicitous correction
    private static boolean isTypeIVError(DMProtocol.Value value) {
        return value.taken && !value.cor.equalsIgnoreCase(value.gt.gt);
    }

    private static boolean hasGoodCandidate(DMProtocol.Value value) {
        if (value.rankings == null) {
            return false;
        }
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
        int typeI = 0;   // no correction candidate
        int typeII = 0;  // good correction not on rank one
        int typeIII = 0; // missed opportunity
        int typeIV = 0;  // infelicitous correction
        int correctBefore = 0;
        int correctAfter = 0;
        int successfulCorrections = 0;
        int skipped = 0;
        int tooShort = 0;
        int n = 0;
    }
}
