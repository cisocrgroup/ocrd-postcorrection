package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;

public class MaxCharNGramsFeature extends NamedCharacterNGramFeature {
    public MaxCharNGramsFeature(String name, FreqMap<String> ngrams) {
        super(name, ngrams);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesAnyOCR(i, n);
    }

    @Override
    public Object calculate(Token token, int i, int n) {
        double max = Double.MIN_VALUE;
        final Word word = i == 0 ? token.getMasterOCR() : token.getOtherOCRAt(i - 1);
        for (String trigram : splitIntoCharacterNGrams(word.toString(), 3)) {
            final double val = getNgrams().getRelative(trigram);
            if (val > max) {
                max = val;
            }
        }
        return max;
    }
}
