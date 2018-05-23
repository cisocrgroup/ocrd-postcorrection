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
}
