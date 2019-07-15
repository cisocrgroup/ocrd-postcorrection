package de.lmu.cis.ocrd.ml.features;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class FeatureSet implements Iterable<Feature>, Serializable {
	private static final long serialVersionUID = -4802453739549010404L;
	private final List<Feature> features = new ArrayList<>();
	private final Vector vector = new Vector(10);

	public Feature get(int i) {
		return features.get(i);
	}

	public FeatureSet add(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public Vector calculateFeatureVector(OCRToken token, int n) {
		vector.clear();
		int j = 0;
		Logger.debug("features for token: {}", token.toString());
		for (Feature feature : this.features) {
			for (int i = 0; i < n; i++) {
				if (!feature.handlesOCR(i, n)) {
					continue;
				}
				final Object val = feature.calculate(token, i, n);
				Logger.debug(" - value for feature {}: {}", feature.getName(), val.toString());
				vector.add(j++, val);
			}
		}
		return vector;
	}

	public int size() {
		return this.features.size();
	}

	@Override
	public Iterator<Feature> iterator() {
		return this.features.iterator();
	}

	public static class Vector extends ArrayList<Object> {
		private static final long serialVersionUID = 4013744915440870424L;

		Vector(int n) {
			super(n);
		}

		@Override
		public String toString() {
			StringJoiner sj = new StringJoiner(",");
			for (Object feature : this) {
				sj.add(feature.toString());
			}
			return sj.toString();
		}

		public void writeCSVLine(PrintWriter w) {
			w.println(this.toString());
		}

		public String toJSON() {
			return new Gson().toJson(this);
		}
	}

}
