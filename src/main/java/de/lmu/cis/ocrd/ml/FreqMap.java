package de.lmu.cis.ocrd.ml;

import java.util.HashMap;

public class FreqMap {
	private final HashMap<String, Integer> frequencies = new HashMap<>();
	private int total;

	public int add(String t) {
		return add(t, 1);
	}

	public int add(String t, int n) {
		int x = doGet(t);
		x += n;
		total += n;
		frequencies.put(t.toLowerCase(), x);
		return x;
	}

	public int getAbsolute(String t) {
		return doGet(t);
	}

	public double getRelative(String t) {
		if (total == 0) {
			return 0.0;
		}
		return (double) doGet(t) / (double) total;
	}

	public int getTotal() {
		return total;
	}

	private int doGet(String t) {
		Integer n = frequencies.get(t.toLowerCase());
		return n == null ? 0 : n;
	}

	public double[] getRelativeNGrams(String t, int n) {
		return calculateNGramRelFreqs(t, n);
	}

	private double[] calculateNGramRelFreqs(String str, int n) {
		str = '$' + str + '$';
		final int[] codepoints = str.codePoints().toArray();
		final int max = codepoints.length - n;
		double[] res = new double[max+1];
		for (int i = 0; i <= max; i++) {
			final String ngram = new String(codepoints, i, n);
			res[i] = getRelative(ngram);
		}
		return res;
	}
}
