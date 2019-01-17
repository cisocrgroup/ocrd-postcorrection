package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.features.OCRWord;

public class EmptyWord implements OCRWord {
	static final OCRWord instance = new EmptyWord();

	private EmptyWord() {}

	@Override
	public boolean isLastInLine() {
		return false;
	}
	@Override
	public boolean isFirstInLine() {
		return false;
	}
	@Override
	public String getLineNormalized() {
		return "";
	}

	@Override
	public double getConfidence() {
		return 0.0;
	}

	@Override
	public double getCharacterConfidenceAt(int i) {
		return 0.0;
	}
	@Override
	public String getWord() {
		return "";
	}

	@Override
	public String toString() {
		return getWord();
	}

}
