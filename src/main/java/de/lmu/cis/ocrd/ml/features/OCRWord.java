package de.lmu.cis.ocrd.ml.features;

public interface OCRWord {
	String getWord();
	String getLineNormalized();
	double getCharacterConfidenceAt(int i);
	double getConfidence();
	boolean isFirstInLine();
	boolean isLastInLine();
}
