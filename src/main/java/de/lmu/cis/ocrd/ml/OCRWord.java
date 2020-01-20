package de.lmu.cis.ocrd.ml;

public interface OCRWord {
	String id();
	String getWordNormalized();
	String getLineNormalized();
	String getWordRaw();
	double getCharacterConfidenceAt(int i);
	double getConfidence();
	boolean isFirstInLine();
	boolean isLastInLine();
}
