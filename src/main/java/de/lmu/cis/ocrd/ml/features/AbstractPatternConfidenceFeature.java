package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.profile.PosPattern;

abstract class AbstractPatternConfidenceFeature extends NamedDoubleFeature {
    AbstractPatternConfidenceFeature(String name) {
        super(name);
    }

    // Calculate the OCR confidence for the given pattern.
    // OCR:        seniper
    // Suggestion: semper
    // Pattern:    m:ni:2
    // c(i):       get confidence for OCR at pos i
    // Result:     c(2) * c(3)
    // ======
    // NOTES:
    // ======
    // Pattern:    a:b  -> substitution
    // Pattern:    a:"" -> insertion ("" means empty string)
    // Pattern:    "":a -> deletion
    static double calculateConfidence(OCRWord word, PosPattern pattern) {
        if (pattern.Right.isEmpty()) {
            return calculateConfidenceForInsertion(word, pattern);
        }
        return calculateConfidenceForSubstitutionOrDeletion(word, pattern);
    }

    // Use the product of the substituted/deleted characters.
    private static double calculateConfidenceForSubstitutionOrDeletion(OCRWord word, PosPattern pattern) {
        double ret = 1;
        final int[] cp = pattern.Right.codePoints().toArray();
        for (int i = 0; i < cp.length; i++) {
            ret *= word.getCharacterConfidenceAt(pattern.Pos + i);
        }
        return ret;
    }

    // There are three possible positions for insertions:
    // at 0:           use confidence for character at pos=0
    // if pos+1 < len: use confidence for the surrounding characters (c(pos)+c(pos+1))/2
    // else :          use confidence for last character at pos=len-1
    private static double calculateConfidenceForInsertion(OCRWord word, PosPattern pattern) {
        if (pattern.Pos == 0) {
            return word.getCharacterConfidenceAt(0);
        }
        final int[] cp = word.getWordNormalized().codePoints().toArray();
        if (pattern.Pos+1 < cp.length) {
            return (word.getCharacterConfidenceAt(pattern.Pos) + word.getCharacterConfidenceAt(pattern.Pos + 1)) / 2.0;
        }
        return word.getCharacterConfidenceAt(cp.length-1);
    }
}
