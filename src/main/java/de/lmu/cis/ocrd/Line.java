package de.lmu.cis.ocrd;

import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;

public class Line extends org.raml.jaxrs.example.model.Line {
  private final Client client;
  private final List<Token> tokens;
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
    List<Token> tmp = client.getTokens(projectId, pageId, lineId);
    if (tmp != null) {
      this.tokens = tmp;
    } else {
      this.tokens = new ArrayList<Token>();
    }
  }

  public String getNormalized() {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (Token t : tokens) {
      if (first) {
        first = false;
      } else {
        builder.append(' ');
      }
      builder.append(t.getCor());
    }
    return builder.toString();
  }
}
