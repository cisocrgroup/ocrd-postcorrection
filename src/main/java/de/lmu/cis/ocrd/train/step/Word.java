package de.lmu.cis.ocrd.train.step;

import java.util.Optional;

import de.lmu.cis.ocrd.SimpleLine;

public class Word implements de.lmu.cis.ocrd.ml.features.Word {
	private final String word;
	private final String line;

	public Word(String word, String line) {
		this.word = word;
		this.line = line;
	}

	@Override
	public boolean isLastInLine() {
		return line.endsWith(word);
	}

	@Override
	public boolean isFirstInLine() {
		return line.startsWith(word, 0);
	}

	@Override
	public String getLineNormalized() {
		return line;
	}

	@Override
	public double getConfidenceAt(int i) {
		return 0;
	}

	@Override
	public String getString() {
		return word;
	}

	@Override
	public String toString() {
		return getString();
	}
}
