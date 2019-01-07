package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.features.OCRWord;

import java.util.ArrayList;
import java.util.List;

public class OCRWordImpl implements OCRWord {
	@SuppressWarnings("unused")
	private final Word word;
	private final int i;
	private final List<String> words;
	private final List<Double> masterOCRCharConfidences;
	private final double wordConfidence;
	private final String line;

	// TODO: improve implementation: OCRWordImpl should not calculate the
	// list of words every time; instead let OCRTokenImpl gather the data once.
	public OCRWordImpl(int i, Word word) {
		this.word = word;
		this.words = word.getUnicodeNormalized();
		this.i = i;
		this.line = word.getParentLine().getUnicodeNormalized().get(i);
		this.wordConfidence = word.getTextEquivs().get(this.i).getConfidence();
		masterOCRCharConfidences = new ArrayList<>();
		for (Glyph g : word.getGlyphs()) {
			final List<TextEquiv> tes = g.getTextEquivs();
			if (tes == null || tes.size() == 0) {
				masterOCRCharConfidences.add(0.0);
			} else {
				masterOCRCharConfidences.add(tes.get(0).getConfidence());
			}
		}
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
		return words.get(i);
	}

	@Override
	public String toString() {
		return getWord();
	}

}
