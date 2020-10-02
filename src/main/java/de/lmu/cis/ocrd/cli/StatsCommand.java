package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.TokenFilter;
import de.lmu.cis.ocrd.profile.*;
import org.pmw.tinylog.Logger;

import java.util.StringJoiner;

public class StatsCommand extends ParametersCommand {
    private int total;         // total number of tokens
    private int lexical;       // number of lexical tokens
    private int strictLexical; // stricter number of lexical tokens
    private int ocrCorrect;    // number of correct ocr tokens (master ocr)
    private double ocrConfSum; // sum of word confidences

    StatsCommand() {
        super("stats");
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        init(config);
        config.setCommand(this);
        execute(config.mustGetInputFileGroups());
    }

    private void execute(String[] ifgs) throws Exception {
        System.out.println(getHeader());
        for (String ifg: ifgs) {
            Logger.debug("ifg = {}", ifg);
            resetCounts();
            final Profile profile = getProfile(ifg, getAlex(), parameters.getNOCR());
            workspace.getNormalTokenReader(ifg, profile).read().stream().filter(TokenFilter::isLong).forEach(this::count);
            System.out.println(getStats(ifg));
        }
    }

    private void count(OCRToken token) {
        Logger.debug("counting token {}", token);
        total++;
        if (token.getCandidate().isPresent()) {
            final Candidate candidate = token.getCandidate().get();
            if (candidate.Distance == 0) { // lexical tokens are tokens that have no ocr error but maybe hist patterns
                Logger.debug(" * token {} is lexical", token);
                lexical++;
            }
            if (candidate.isLexiconEntry()) {
                strictLexical++;
            }
        }
        ocrConfSum += token.getMasterOCR().getConfidence();
        if (token.getGT().isPresent()) {
            if (token.getMasterOCR().getWordNormalized().toLowerCase().equals(token.getGT().get().toLowerCase())) {
                Logger.debug(" * token {} is correct", token);
                ocrCorrect++;
            }
        }
    }

    private String getHeader() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("input file group");
        sj.add("word accuracy");
        sj.add("lexicality");
        sj.add("strict lexicality");
        sj.add("average ocr word confidence");
        return sj.toString();
    }

    private String getStats(String ifg) {
        if (total == 0) {
            total = 1; // fix NaNs (if total = 0, all other counts will be zero as well)
        }
        StringJoiner sj = new StringJoiner(",");
        sj.add(ifg);
        sj.add(Double.toString((double)ocrCorrect/(double)total));
        sj.add(Double.toString((double)lexical/(double)total));
        sj.add(Double.toString((double)strictLexical/(double)total));
        sj.add(Double.toString(ocrConfSum/total));
        return sj.toString();
     }

    private void resetCounts() {
        total = 0;
        lexical = 0;
        strictLexical = 0;
        ocrConfSum = 0;
        ocrCorrect = 0;
    }

    private AdditionalLexicon getAlex() {
        return parameters.isRunLE()?
                new AdditionalFileLexicon(parameters.getLETraining().getLexicon(parameters.getNOCR())):
                new NoAdditionalLexicon();
    }
}
