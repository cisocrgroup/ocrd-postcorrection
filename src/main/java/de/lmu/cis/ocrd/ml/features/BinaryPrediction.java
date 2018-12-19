package de.lmu.cis.ocrd.ml.features;

public interface BinaryPrediction {
	boolean getPrediction();
	double getConfidence();
}
