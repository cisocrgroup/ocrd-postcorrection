package de.lmu.cis.ocrd.ml;

import java.util.ArrayList;
import java.util.List;

public class FeatureSet {
	public interface Callback {
		void apply(Value v);
	}

	private final List<Feature> features = new ArrayList<Feature>();

	public FeatureSet add(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public ArrayList<Value> calculateFeatureVector(Token token) {
		ArrayList<Value> vec = new ArrayList<Value>(this.size());
		each(token, (Value v) -> {
			vec.add(v);
		});
		return vec;
	}

	public void each(Token token, Callback callback) {
		for (Feature feature : this.features) {
			callback.apply(feature.calculate(token));
		}
	}

	public int size() {
		return this.features.size();
	}
}
