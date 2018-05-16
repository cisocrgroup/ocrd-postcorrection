package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class ScreamCaseFeature extends NamedBooleanFeature {
    public ScreamCaseFeature(String name) {
        super(name);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }

    @Override
    protected boolean doCalculate(Token token, int i, int n) {
        final byte uppercaseLetter = Character.UPPERCASE_LETTER;
        for (int c : token.getMasterOCR().toString().codePoints().toArray()) {
            if (Character.getType(c) != uppercaseLetter) {
                return false;
            }
        }
        return true;
    }
}
