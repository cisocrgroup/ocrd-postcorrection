package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.FeatureSet;

import java.io.Serializable;

public interface Classifier extends Serializable {
	Prediction classify(FeatureSet.Vector features) throws Exception;
}
