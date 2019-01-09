package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.features.OCRWord;

import java.util.List;

public class OCRWordImpl implements OCRWord {
	private final String word;
	private final List<Double> masterOCRCharConfidences;
	private final double wordConfidence;
	private final String line;

	public OCRWordImpl(TextEquiv te, String line, List<Double> mConfs) {
		this.line = line;
		this.wordConfidence = te.getConfidence();
		this.word = te.getUnicodeNormalized();
		this.masterOCRCharConfidences = mConfs;
	}

	@Override
	public boolean isLastInLine() {
		return line.endsWith(getWord());
	}

	@Override
	public boolean isFirstInLine() {
		return line.startsWith(getWord(), 0);
	}

	@Override
	public String getLineNormalized() {
		return line;
	}

	@Override
	public double getConfidence() {
		return wordConfidence;
	}

	@Override
	public double getCharacterConfidenceAt(int i) {
		if (i < masterOCRCharConfidences.size()) {
			return masterOCRCharConfidences.get(i);
		}
		return 0.0;
	}

	@Override
	public String getWord() {
		return word;
	}

	@Override
	public String toString() {
		return getWord();
	}

}
