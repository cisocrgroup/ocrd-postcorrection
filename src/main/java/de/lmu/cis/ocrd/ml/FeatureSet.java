package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Feature;

import java.util.ArrayList;
import java.util.List;

public class FeatureSet {
	public interface Callback {
		void apply(double v);
	}

	public interface FeatureCallback {
	    void apply(Feature f);
    }

	private final List<Feature> features = new ArrayList<Feature>();

	public FeatureSet add(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public ArrayList<Double> calculateFeatureVector(Token token) {
		ArrayList<Double> vec = new ArrayList<>(this.size());
		each(token, (double v) -> {
			vec.add(v);
		});
		return vec;
	}

	public void each(Token token, Callback callback) {
		for (Feature feature : this.features) {
            try {
                callback.apply(feature.calculate(token, -1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	public void each(FeatureCallback cb) {
		for (Feature feature : features) {
			cb.apply(feature);
		}
	}

	public int size() {
		return this.features.size();
	}
}
