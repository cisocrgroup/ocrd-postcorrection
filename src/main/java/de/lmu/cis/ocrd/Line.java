package de.lmu.cis.ocrd;

import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;

public class Line extends org.raml.jaxrs.example.model.Line {
  private final Client client;
  private final List<Token> tokens;
  private final String normalized;
  private final List<Token> tokenAlignements;
  public Line(Client client, org.raml.jaxrs.example.model.Line line)
      throws Exception {
    this.withProjectId(line.getProjectId())
        .withPageId(line.getPageId())
        .withLineId(line.getLineId())
        .withOcr(line.getOcr())
        .withCor(line.getCor())
        .withCuts(line.getCuts())
        .withConfidences(line.getConfidences())
        .withAverageConfidence(line.getAverageConfidence())
        .withIsFullyCorrected(line.getIsFullyCorrected())
        .withIsPartiallyCorrected(line.getIsPartiallyCorrected())
        .withBox(line.getBox());
    this.client = client;
    this.tokens = loadTokens();
    Pair pair = getNormalizedData();
    this.normalized = pair.normalized;
    this.tokenAlignements = pair.tokenAlignements;
  }

  public String getNormalized() { return normalized; }

  public Token getTokenAt(int i) { return tokenAlignements.get(i); }
  public List<Token> getTokens() { return tokenAlignements; }

  private class Pair {
    public Pair(String normalized, List<Token> tokenAlignements) {
      this.normalized = normalized;
      this.tokenAlignements = tokenAlignements;
    }
    public String normalized;
    public List<Token> tokenAlignements;
  }
  private Pair getNormalizedData() {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    List<Token> alignements = new ArrayList<Token>();
    for (Token t : tokens) {
      if (first) {
        first = false;
      } else {
        builder.append(' ');
        alignements.add(null); // there is surely a better way
      }
      builder.append(t.getCor());
      for (int i = 0; i < t.getCor().length(); i++) {
        alignements.add(t);
      }
    }
    return new Pair(builder.toString(), alignements);
  }

  private List<Token> loadTokens() throws Exception {
    List<Token> tokens = client.getTokens(projectId, pageId, lineId);
    if (tokens == null) {
      return new ArrayList<Token>();
    }
    return tokens;
  }
}
