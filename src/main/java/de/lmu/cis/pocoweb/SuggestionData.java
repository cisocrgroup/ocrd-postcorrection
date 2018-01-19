package de.lmu.cis.pocoweb;

class SuggestionData {
  public int projectId;
  public int pageId;
  public int lineId;
  public int tokenId;
  public String token;
  public String suggestion;
  public double weight;
  public int distance;
  public String[] ocrPatterns;
  public String[] histPatterns;
  public boolean isTopSuggestion;
  public boolean isOcrError;
}
