package de.lmu.cis.ocrd.ml;

import java.io.Serializable;

public interface Classifier extends Serializable {
	Prediction predict(FeatureSet.Vector features) throws Exception;
}
