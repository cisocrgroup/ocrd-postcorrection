package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.Feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FeatureSet implements Iterable<Feature>, Serializable {

	private final List<Feature> features = new ArrayList<>();

	public Feature get(int i) {
		return features.get(i);
	}

	public FeatureSet add(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public Vector calculateFeatureVector(Token token, int n) {
		Vector vec = new Vector(this.size());
		for (Feature feature : this.features) {
			for (int i = 0; i < n; i++) {
				if (!feature.handlesOCR(i, n)) {
					continue;
				}
				vec.add(feature.calculate(token, i, n));
			}
		}
		return vec;
	}

	public int size() {
		return this.features.size();
	}

	@Override
	public Iterator<Feature> iterator() {
		return this.features.iterator();
	}

	public static class Vector extends ArrayList<Object> {
		Vector(int n) {
			super(n);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (Object feature : this) {
				if (!first) {
					builder.append(',');
				}
				builder.append(feature.toString());
				first = false;
			}
			return builder.toString();
		}

		public String toJSON() {
			return new Gson().toJson(this);
		}
	}

}
