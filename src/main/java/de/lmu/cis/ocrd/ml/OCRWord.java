package de.lmu.cis.ocrd.ml;

public interface OCRWord {
	String getID();
	String getWordNormalized();
	String getLineNormalized();
	String getWordRaw();
	double getCharacterConfidenceAt(int i);
	double getConfidence();
}
