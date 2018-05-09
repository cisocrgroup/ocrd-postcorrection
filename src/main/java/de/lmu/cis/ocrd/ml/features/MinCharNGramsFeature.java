package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;

public class MinCharNGramsFeature extends NamedCharacterNGramFeature {
    public MinCharNGramsFeature(String name, FreqMap<String> ngrams) {
        super(name, ngrams);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        // handles any OCR
        return true;
    }

    @Override
    public double calculate(Token token, int i, int n) {
        double min = Double.MAX_VALUE;
        final Word word = i == 0 ? token.getMasterOCR() : token.getOtherOCRAt(i - 1);
        for (String trigram : splitIntoCharacterNGrams(word.toString(), 3)) {
            final double val = getNgrams().getRelative(trigram);
            if (val < min) {
                min = val;
            }
        }
        return min;
    }
}
