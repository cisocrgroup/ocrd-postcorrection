package de.lmu.cis.ocrd.ml.features;

import java.util.Optional;

public interface OCRToken {
	public OCRWord getMasterOCR();

	public OCRWord getOtherOCR(int i);

	public Optional<String> getGT();

}
