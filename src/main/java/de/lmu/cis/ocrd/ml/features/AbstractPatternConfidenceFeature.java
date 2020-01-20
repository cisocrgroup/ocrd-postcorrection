package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.profile.PosPattern;
import de.lmu.cis.ocrd.util.JSON;

public abstract class AbstractPatternConfidenceFeature extends NamedDoubleFeature {
    public AbstractPatternConfidenceFeature(JsonObject o, ArgumentFactory args) {
        this(JSON.mustGetNameOrType(o));
    }

    AbstractPatternConfidenceFeature(String name) {
        super(name);
    }

    // Calculate the OCR confidence for the given pattern.
    // OCR:       seniper
    // Candidate: semper
    // Pattern:   m:ni:2
    // c(i):      get confidence for OCR at pos i
    // Result:    c(2) * c(3)
    double calculateConfidence(OCRWord word, PosPattern pattern) {
        if (pattern.Left.isEmpty()) {
            return calculateConfidenceForInsertion(word, pattern);
        }
        return calculateConfidenceForSubstitution(word, pattern);
    }

    // Use the product of the substituted characters.
    private static double calculateConfidenceForSubstitution(OCRWord word, PosPattern pattern) {
        double ret = 1;
        final int[] cp = pattern.Left.codePoints().toArray();
        for (int i = 0; i < cp.length; i++) {
            ret *= word.getCharacterConfidenceAt(pattern.Pos + i);
        }
        return ret;
    }

    // There are three possible positions:
    // at 0: use confidence for character at pos=0
    // at len: use confidence for last character at pos=len-1
    // else: use confidence for surrounding characters (pos+pos-1)/2
    private static double calculateConfidenceForInsertion(OCRWord word, PosPattern pattern) {
        if (pattern.Pos == 0) {
            return word.getCharacterConfidenceAt(0);
        }
        final int[] cp = word.getWordNormalized().codePoints().toArray();
        if (pattern.Pos > cp.length) {
            return word.getCharacterConfidenceAt(cp.length-1);
        }
        return (word.getCharacterConfidenceAt(pattern.Pos) + word.getCharacterConfidenceAt(pattern.Pos - 1)) / 2.0;
    }
}
