package de.lmu.cis.ocrd.ml.features;

public interface OCRWord {
	String getWord();
	String getLineNormalized();
	double getConfidenceAt(int i);
	boolean isFirstInLine();
	boolean isLastInLine();
}
