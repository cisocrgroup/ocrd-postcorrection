package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

// Feature represents the feature for the training and evaluation of a document.
// There are features for the master OCR (isAdditionalOCRFeature() returns false) and
// features for additional features (isAdditionalOCRFeature() returns true).
public interface Feature {
    // Returns the name of the feature.
	String getName();

	// Returns whether the feature is a feature for additional OCRs.
    // If true, this feature is used multiple times if multiple OCRs are in use
    // but not for the master OCR.
    boolean isAdditionalOCRFeature();

	// Calculate the value of a feature.
    // The index of `additional OCR` gives the index of the currently active
    // additional OCR or -1 if the master OCR is in use.
    // Additional OCR features (see above) should *never* be used for the master OCR.
    // Master OCR features should *never* be used for additional OCRs.
	double calculate(Token token, int additionalOCR);
}
