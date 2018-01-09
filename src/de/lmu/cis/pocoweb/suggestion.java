package de.lmu.cis.pocoweb;

class Suggestion {
  public int pageId;
  public int lineId;
  public int tokenId;
  public String token;
  public String suggestion;
  public double weight;
  public int distance;
  public String[] patterns;
}
