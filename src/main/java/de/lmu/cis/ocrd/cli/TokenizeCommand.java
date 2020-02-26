package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;

class TokenizeCommand extends ParametersCommand {

    TokenizeCommand() {
        super("tokenize");
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        init(config);
        config.setCommand(this);
        final String[] ifgs = config.mustGetInputFileGroups();
        for (String ifg: ifgs) {
            tokenize(ifg);
        }
    }

    private void tokenize(String ifg) throws Exception {
        final Profile profile = getProfile(ifg, new NoAdditionalLexicon(), getParameters().getNOCR());
        for (OCRToken token: workspace.getNormalTokenReader(ifg, profile).read()) {
            System.out.printf("%s: %s\n", ifg, token.toString());
        }
    }
}
