package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.OCRWord;

public class OCRTokenImpl implements OCRToken {

	private final List<OCRWordImpl> words;
	private final boolean withGT;

	public OCRTokenImpl(Word word, boolean withGT) {
		this.withGT = withGT;
		this.words = new ArrayList<>();
		for (int i = 0; i < word.getUnicode().size(); i++) {
			words.add(new OCRWordImpl(i, word));
		}
	}

	@Override
	public OCRWord getMasterOCR() {
		if (!withGT) {
			return words.get(0);
		}
		return words.get(1);
	}

	@Override
	public OCRWord getOtherOCR(int i) {
		if (!withGT) {
			return words.get(i + 1);
		}
		return words.get(i + 2);
	}

	@Override
	public Optional<String> getGT() {
		if (!withGT) {
			return Optional.empty();
		}
		return Optional.of(this.words.get(0).getWord());
	}
}
