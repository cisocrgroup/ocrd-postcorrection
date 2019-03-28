package de.lmu.cis.ocrd.ml.features;

import com.google.gson.Gson;
import org.pmw.tinylog.Logger;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FeatureSet implements Iterable<Feature>, Serializable {
	private static final long serialVersionUID = -4802453739549010404L;
	private final List<Feature> features = new ArrayList<>();

	public Feature get(int i) {
		return features.get(i);
	}

	public FeatureSet add(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public Vector calculateFeatureVector(OCRToken token, int n) {
		Vector vec = new Vector(this.size());
		for (Feature feature : this.features) {
			for (int i = 0; i < n; i++) {
				if (!feature.handlesOCR(i, n)) {
					continue;
				}
				Logger.debug("feature {}", feature.getName());
				final Object val = feature.calculate(token, i, n);
				Logger.debug("value for feature {}: {}", feature.getName(), val.toString());
				vec.add(val);
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
		private static final long serialVersionUID = 4013744915440870424L;

		public Vector(int n) {
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

		public void writeCSVLine(PrintWriter w) {
			boolean first = true;
			for (Object feature : this) {
				if (!first) {
					w.print(',');
				}
				w.print(feature);
				first = false;
			}
			w.println();
		}

		public String toJSON() {
			return new Gson().toJson(this);
		}
	}

}
