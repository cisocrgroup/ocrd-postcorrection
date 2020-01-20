package de.lmu.cis.ocrd.ml;

import java.util.Optional;

public interface BaseOCRToken {
    // Return the number of ocrs (including master ocr).
    int getNOCR();
    // Get master ocr result.
    OCRWord getMasterOCR();
    // Get result of a slave ocr (index starts at 0 e.g. the
    // index of the second OCR (first slave OCR) is 0.
    OCRWord getSlaveOCR(int i);
    // Get the GT string (if it exists).
    Optional<String> getGT();
    // Returns true if the ocr token is a lexicon entry.
    boolean isLexiconEntry();
    // Return true if the master OCR token is correct.
    boolean ocrIsCorrect();
    // Correct this token in the underlying document.
    void correct(String correction, double confidence);
}
