package de.lmu.cis.ocrd;

import de.lmu.cis.ocrd.ml.features.OCRWord;

import java.io.Serializable;
import java.util.Optional;

public class Word implements Serializable, OCRWord {
	private static final long serialVersionUID = 2254894331400744361L;
	private static final int SMALL_WORD = 3;
	private final SimpleLine line;
	private final int s, e;

	public Word(int s, int e, SimpleLine line) {
		this.s = s;
		this.e = e;
		this.line = line;
	}

	public static Word create(String str) {
		Optional<Word> word = SimpleLine.normalized(str, 0.0).getWord(str);
		assert (word.isPresent());
		return word.get();
	}

	public static Word empty(SimpleLine line) {
		return new Word(0, 0, line);
	}

	public boolean isLastInLine() {
		return this.e == this.line.getNormalized().length();
	}

	public boolean isFirstInLine() {
		return this.s == 0;
	}

	public SimpleLine getLine() {
		return line;
	}

	public int getSize() {
		return e - s;
	}

	public double getCharacterConfidenceAt(int i) {
		return line.getConfidenceAt(s + i);
	}

	@Override
	public String id() {
		return Integer.toString(line.getLineId());
	}

	@Override
	public double getConfidence() {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = 0; i < getSize(); i++) {
			min = Double.min(min, getCharacterConfidenceAt(i));
			max = Double.max(max, getCharacterConfidenceAt(i));
		}
		return (min + max) / 2.0;
	}

	@Override
	public String toString() {
		return line.getNormalized().substring(s, e);
	}

	public boolean isShort() {
		return getSize() <= SMALL_WORD;
	}

	@Override
	public String getWord() {
		return toString();
	}

	@Override
	public String getLineNormalized() {
		return getLine().getNormalized();
	}
}
