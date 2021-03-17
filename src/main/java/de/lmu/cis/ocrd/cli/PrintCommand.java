package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRTokenReader;
import de.lmu.cis.ocrd.ml.TokenFilter;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

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
            Logger.debug("loaded {} tokens", tokens.size());
            TokenFilter.filter(tokens, (t)-> t.getGT().isPresent()).forEach(token->{
                assert(TokenFilter.isLong(token));
                assert(token.getGT().isPresent());
                Logger.debug("nocr = {}", token.getNOCR());
                System.out.printf("%s: %s", token.getID(), token.getMasterOCR().getWordNormalized());
                for (int i = 1; i < token.getNOCR(); i++) {
                    System.out.printf(" %s", token.getSlaveOCR(i-1));
                }
                if (token.getGT().isPresent()) {
                    System.out.printf(" %s", token.getGT().get());
                }
                System.out.println();
            });
        }
    }
}
