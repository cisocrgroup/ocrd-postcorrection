package de.lmu.cis.ocrd.ml.features;

import weka.core.Instance;

import java.util.List;

public interface BinaryPredictor {
	BinaryPrediction predict(List<Object> featureValues) throws Exception;
	BinaryPrediction predict(Instance instance) throws Exception;
}
