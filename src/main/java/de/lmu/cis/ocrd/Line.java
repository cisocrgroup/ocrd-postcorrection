package de.lmu.cis.ocrd;

import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;

public class Line extends org.raml.jaxrs.example.model.Line {
  private final Client client;
  private final List<Token> tokens;
  private String normalized;
  private List<Token> tokenAlignements;
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
    this.tokens = getTokens();
    normalized = null;
    tokenAlignements = null;
    // List<Token> tmp = client.getTokens(projectId, pageId, lineId);
    // if (tmp != null) {
    //   this.tokens = tmp;
    // } else {
    //   this.tokens = new ArrayList<Token>();
    // }
  }

  public String getNormalized() {
    if (normalized == null || tokenAlignements == null) {
      calculateNormalizedData();
    }
    return normalized;
  }

  public Token getTokenAt(int i) { return tokenAlignements.get(i); }

  private void calculateNormalizedData() {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    tokenAlignements = new ArrayList<Token>();
    for (Token t : tokens) {
      if (first) {
        first = false;
      } else {
        builder.append(' ');
        tokenAlignements.add(null); // there is surely a better way
      }
      builder.append(t.getCor());
      for (int i = 0; i < t.getCor().length(); i++) {
        tokenAlignements.add(t);
      }
    }
    normalized = builder.toString();
  }

  private List<Token> getTokens() throws Exception {
    List<Token> tokens = client.getTokens(projectId, pageId, lineId);
    if (tokens == null) {
      return new ArrayList<Token>();
    }
    return tokens;
  }
}
