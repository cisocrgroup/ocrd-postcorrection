package de.lmu.cis.ocrd.ml.features;

public interface BinaryPredictor {
	BinaryPrediction predict(OCRToken token);
}
