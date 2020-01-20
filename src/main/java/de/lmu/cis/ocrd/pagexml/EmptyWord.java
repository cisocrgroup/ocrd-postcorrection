package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.OCRWord;

public class EmptyWord implements OCRWord {
	static final OCRWord instance = new EmptyWord();

	private EmptyWord() {}

	@Override
	public String id() {return "empty";}
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
	public String getWordRaw() {
		return getWordNormalized();
	}

	@Override
	public String getWordNormalized() {
		return "**EMPTY-WORD**";
	}

	@Override
	public String toString() {
		return getWordNormalized();
	}

}
