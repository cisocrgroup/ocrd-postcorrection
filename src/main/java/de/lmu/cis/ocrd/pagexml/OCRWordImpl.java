package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.features.OCRWord;

import java.util.List;

public class OCRWordImpl implements OCRWord {
	@SuppressWarnings("unused")
	private final Word word;
	private final int i;
	private final List<String> words;
	private final String line;

	public OCRWordImpl(int i, Word word) {
		this.word = word;
		this.words = word.getUnicodeNormalized();
		this.i = i;
		this.line = word.getParentLine().getUnicodeNormalized().get(i);
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
	public double getConfidenceAt(int i) {
		return word.getTextEquivs().get(this.i).getConfidence();
	}

	@Override
	public String getWord() {
		return words.get(i);
	}

	@Override
	public String toString() {
		return getWord();
	}

}
