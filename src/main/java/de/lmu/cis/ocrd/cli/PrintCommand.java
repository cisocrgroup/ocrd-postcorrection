package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRTokenReader;
import de.lmu.cis.ocrd.profile.Profile;

import java.util.List;

public class PrintCommand extends ParametersCommand {

    PrintCommand() {
        super("print");
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        init(config);
        config.setCommand(this);
        final String[] ifgs = config.mustGetInputFileGroups();
        final Profile emptyProfile = Profile.empty();
        for (String ifg: ifgs) {
            OCRTokenReader r = workspace.getNormalTokenReader(ifg, emptyProfile);
            final List<OCRToken> tokens = r.read();
            tokens.forEach(token->{
                System.out.printf("%s: %s", token.getID(), fixWhiteSpace(token.getMasterOCR().getWordNormalized()));
                for (int i = 1; i < token.getNOCR(); i++) {
                    System.out.printf(" %s", fixWhiteSpace(token.getSlaveOCR(i - 1).getWordNormalized()));
                }
                if (token.getGT().isPresent()) {
                    System.out.printf(" %s", fixWhiteSpace(token.getGT().get()));
                }
                System.out.println();
            });
        }
    }

    private static String fixWhiteSpace(String str) {
        return str.replace(' ', '_');
    }
}
