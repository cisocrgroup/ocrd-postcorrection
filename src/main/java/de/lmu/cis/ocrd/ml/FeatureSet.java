package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FeatureSet implements Iterable<Feature> {

	private final List<Feature> features = new ArrayList<>();

	public FeatureSet add(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public List<Double> calculateFeatureVector(Token token) {
		return calculateFeatureVector(token, 1);
	}

	public List<Double> calculateFeatureVector(Token token, int n) {
		ArrayList<Double> vec = new ArrayList<>(this.size());
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

}
