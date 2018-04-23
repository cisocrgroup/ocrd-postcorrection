package de.lmu.cis.ocrd.ml;

import java.util.ArrayList;
import java.util.List;

import de.lmu.cis.pocoweb.Token;

public class FeatureSet {
	public interface Callback {
		void apply(double d);
	}

	private final List<Feature> features = new ArrayList<Feature>();

	public FeatureSet add(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public ArrayList<Double> calculateFeatureVector(Token token) {
		ArrayList<Double> vec = new ArrayList<Double>(this.size());
		each(token, (double d) -> {
			vec.add(d);
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
