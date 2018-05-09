package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public class TitleCaseFeature extends NamedBooleanMasterOCRFeature {
    public TitleCaseFeature(String name) {
        super(name);
    }

    @Override
    protected boolean doCalculate(Token token) {
        byte want = Character.UPPERCASE_LETTER;
        for (int c : token.getMasterOCR().toString().codePoints().toArray()) {
            if (Character.getType(c) != want) {
                return false;
            }
            want = Character.LOWERCASE_LETTER;
        }
        return true;
    }
}
