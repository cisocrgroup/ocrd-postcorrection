package de.lmu.cis.ocrd.ml.features;

public interface Word {
	String getString();
	String getLineNormalized();
	double getConfidenceAt(int i);
	boolean isFirstInLine();
	boolean isLastInLine();
}
