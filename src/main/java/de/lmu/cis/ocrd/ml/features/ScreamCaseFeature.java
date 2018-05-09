package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class ScreamCaseFeature extends NamedBooleanMasterOCRFeature {
    public ScreamCaseFeature(String name) {
        super(name);
    }

    @Override
    protected boolean doCalculate(Token token) {
        final byte uppercaseLetter = Character.UPPERCASE_LETTER;
        for (int c : token.getMasterOCR().toString().codePoints().toArray()) {
            if (Character.getType(c) != uppercaseLetter) {
                return false;
            }
        }
        return true;
    }
}
