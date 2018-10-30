package de.lmu.cis.ocrd.pagexml;

import java.util.List;

import de.lmu.cis.ocrd.ml.features.OCRWord;

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
		// TODO: not implemented
		return 0;
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
