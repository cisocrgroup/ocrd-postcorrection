package de.lmu.cis.ocrd.ml;

import java.util.Optional;

// Main interface to implement tokens from different sources.
// CandidateOCRToken, CandidatesOCRToken and RankingsOCRTokens
// use this interface to implement the required OCRToken interface.
public interface BaseOCRToken {
    // Return the number of ocrs (including master ocr).
    int getNOCR();
    // Return the unique token id
    String getID();
    // Get master ocr result.
    OCRWord getMasterOCR();
    // Get result of a slave ocr (index starts at 0 e.g. the
    // index of the second OCR (first slave OCR) is 0.
    OCRWord getSlaveOCR(int i);
    // Get the GT string (if it exists).
    Optional<String> getGT();
    // Correct this token in the underlying document.
    void correct(String correction, double confidence);
}
