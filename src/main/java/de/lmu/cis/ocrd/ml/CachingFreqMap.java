package de.lmu.cis.ocrd.ml;

import java.util.HashMap;
import java.util.Map;

public class CachingFreqMap extends FreqMap {
	final Map<String, double[]> cache;

	CachingFreqMap() {
		super();
		cache = new HashMap<>();
	}

	@Override
	public double[] getRelativeNGrams(String t, int n) {
		if (!cache.containsKey(t)) {
			cache.put(t, super.getRelativeNGrams(t, n));
		}
		return cache.get(t);
	}
}
