package de.lmu.cis.ocrd.ml;

import com.google.gson.GsonBuilder;

public class Prediction {
	public final double value;
	private final double[] confidences;
	public final String label;

	Prediction(double value, double[] confidences, String label) {
		this.value = value;
		this.confidences = confidences;
		this.label = label;
	}

	public double getConfidence() {
		assert (((int) value) < confidences.length);
		return confidences[(int) value];
	}

	public String toJSON() {
		// handle NAN values in json
		return new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(this);
	}
}
