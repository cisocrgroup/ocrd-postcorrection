package de.lmu.cis.ocrd.train.step;

import java.util.Optional;

import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.ml.features.OCRToken;

class PageXMLOCRTokenImpl implements OCRToken {

	private final de.lmu.cis.ocrd.pagexml.Word word;
	private final boolean withGT;

	public PageXMLOCRTokenImpl(de.lmu.cis.ocrd.pagexml.Word word, boolean withGT) {
		this.word = word;
		this.withGT = withGT;
	}

	@Override
	public Word getMasterOCR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Word getOtherOCR(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getGT()  {
		if (!this.withGT) {
			return Optional.empty();
		}
		return Optional.of(this.word.getUnicode().get(0));
	}
}
