package de.lmu.cis.ocrd.ml;

import com.google.gson.GsonBuilder;
import de.lmu.cis.ocrd.ml.features.BinaryPrediction;

public class Prediction implements BinaryPrediction {
	private final int value;
	public final String label;
	private final double[] confidences;

	Prediction(double value, double[] confidences, String label) {
		this.value = (int)value;
		this.confidences = confidences;
		this.label = label;
	}


	@Override
	public boolean getPrediction() {
		return value == 0; // 0 is true class, 1 is false class
	}

	@Override
	public double getConfidence() {
		assert(value < confidences.length);
		return confidences[value];
	}

	public String toJSON() {
		// handle NAN values in json
		return new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(this);
	}
}
