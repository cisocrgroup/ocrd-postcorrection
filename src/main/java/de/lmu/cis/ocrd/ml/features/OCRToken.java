package de.lmu.cis.ocrd.ml.features;

import java.util.Optional;

public interface OCRToken {
	public Word getMasterOCR();

	public Word getOtherOCR(int i);

	public Optional<String> getGT();

}
