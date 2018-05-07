package de.lmu.cis.ocrd.ml;

import java.util.HashMap;

public class FreqMap<T> {
  private final HashMap<T, Integer> freqs = new HashMap<>();
  private int total;

  public int add(T t) {
      return add(t, 1);
  }

  public int add(T t, int n) {
      int x = doGet(t);
      x += n;
      total += n;
      freqs.put(t, x);
      return x;
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

  private int doGet(T t) {
    Integer n = freqs.get(t);
    if (n == null) {
      return 0;
    }
    return n;
  }
}
