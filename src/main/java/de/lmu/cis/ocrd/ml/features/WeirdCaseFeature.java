package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class WeirdCaseFeature extends NamedBooleanFeature {
    public WeirdCaseFeature(String name) {
        super(name);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }

    @Override
    protected boolean doCalculate(Token token, int i, int n) {
        byte uppercaseLetter = Character.UPPERCASE_LETTER;
        final byte lowercaseLetter = Character.LOWERCASE_LETTER;
        for (int c : token.getMasterOCR().toString().codePoints().toArray()) {
            final int type = Character.getType(c);
            if (type != uppercaseLetter && type != lowercaseLetter) {
                return false;
            }
            uppercaseLetter = lowercaseLetter; // lower cases only after first iteration
        }
        return true;
    }
}