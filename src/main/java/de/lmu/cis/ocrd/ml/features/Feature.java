package de.lmu.cis.ocrd.ml.features;

import java.io.Serializable;

// Feature represents the feature for the training and evaluation of a document.
// Features are allowed to handle different multiple OCRs.
// The master OCR has index 0. Additional features have an index > 0.
public interface Feature extends Serializable {
	// Returns the name of the feature.
	String getName();

	// Returns the classes of the given feature.  For real numbers it should return REAL,
	// for other classes the array of the class labels e.g. {label-a,label-b,...}
	String getClasses();

	// Returns whether the feature handles the current OCR round.
	// Features that only handle the master OCR handlesOCR(i, n) returns true iff
	// i=0.
	//
	// Features that handle additional OCRs handlesOCR(i, n) can return
	// corresponding values.
	// Any implementation must guarantee that calculate(token, i, n) is only called
	// iff handlesOCR(i, n) = true.
	boolean handlesOCR(int i, int n);

	// Calculate the value of a feature.
	// The index i and the number of OCRs n are given with each call to calculate
	// (see above).
	Object calculate(OCRToken token, int i, int n);
}
