package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.util.StringCorrector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class OCRToken implements de.lmu.cis.ocrd.ml.BaseOCRToken {

	private final Word word;
	private final List<OCRWordImpl> words;
	private final int gtIndex;

	public OCRToken(Word word, int gtIndex) throws Exception {
		this.gtIndex = gtIndex;
		this.word = word;
		this.words = getWords(word, gtIndex);
	}

	private static List<OCRWordImpl> getWords(Word word, int gtIndex) throws Exception {
		List<OCRWordImpl> words = new ArrayList<>();
		final List<TextEquiv> tes = word.getTextEquivs();
		if (tes.isEmpty()) {
			throw new Exception("empty word");
		}
		final List<Double> mConfs = new ArrayList<>();
		for (Glyph g : word.getGlyphs()) {
			final List<TextEquiv> gtes = g.getTextEquivs();
			if (gtes == null || gtes.isEmpty()) {
				mConfs.add(0.0);
			} else {
				mConfs.add(gtes.get(0).getConfidence());
			}
		}
		final List<String> normLines =
				word.getParentLine().getUnicodeNormalized();
		for (int i = 0; i <= gtIndex && i < tes.size() && i < normLines.size(); i++) {
			words.add(new OCRWordImpl(tes.get(i), normLines.get(i), mConfs));
		}
		while (words.size() < gtIndex) {
			words.add(new OCRWordImpl(tes.get(0), normLines.get(0), mConfs));
		}
		return words;
	}

	@Override
	public int getNOCR() {
		return gtIndex;
	}

	@Override
	public OCRWord getMasterOCR() {
		if (0 < words.size()) {
			return words.get(0);
		}
		return EmptyWord.instance;
	}

	@Override
	public OCRWord getSlaveOCR(int i) {
		if (i+1 < words.size()) {
			return words.get(i+1);
		}
		return EmptyWord.instance;
	}

	@Override
	public Optional<String> getGT() {
		if (gtIndex <= 0 || gtIndex >= words.size()) {
			return Optional.empty();
		}
		return Optional.of(words.get(gtIndex).getWordNormalized());
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(",");
		sj.add("mOCR:" + getMasterOCR().toString());
		for (int i = 1; i < getNOCR(); i++) {
			sj.add("OCR" + (i + 1) + ":" + getSlaveOCR(i - 1).toString());
		}
		if (getGT().isPresent()) {
			sj.add("gt:" + getGT().get());
		}
		return sj.toString();
	}

	@Override
	public void correct(String correction, double confidence) {
		word.prependNewTextEquiv()
				.addUnicode(new StringCorrector(getMasterOCR().getWordRaw()).correctWith(correction))
				.withConfidence(confidence)
				.withIndex(0)
				.withDataType("OCR-D-CIS-POST-CORRECTION")
				.withDataTypeDetails(getMasterOCR().getWordRaw());
	}
}
