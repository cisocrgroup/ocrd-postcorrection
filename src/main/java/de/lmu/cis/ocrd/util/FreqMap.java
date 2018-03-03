package de.lmu.cis.ocrd.util;

import java.util.HashMap;

public class FreqMap<T> {
  private final HashMap<T, Integer> freqs = new HashMap<T, Integer>();
  private int total;

  public int add(T t) {
    int n = doGet(t);
    n++;
    freqs.put(t, n);
    total++;
    return n;
  }

  public int getAbsolute(T t) {
    return doGet(t);
  }

  public double getRelative(T t) {
    if (total == 0) {
      return 0.0;
    }
    return (double)doGet(t) / (double)total;
  }

  public int getTotal() {
    return total;
  }

  private final int doGet(T t) {
    Integer n = freqs.get(t);
    if (n == null) {
      n = new Integer(0);
    }
    return n;
  }
}
