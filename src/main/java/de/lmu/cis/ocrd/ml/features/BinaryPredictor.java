package de.lmu.cis.ocrd.ml.features;

import java.util.List;

public interface BinaryPredictor {
	BinaryPrediction predict(List<Object> featureValues) throws Exception;
}
