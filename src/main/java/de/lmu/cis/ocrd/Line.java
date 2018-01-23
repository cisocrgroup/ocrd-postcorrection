package de.lmu.cis.ocrd;

import de.lmu.cis.pocoweb.Client;

public class Line extends org.raml.jaxrs.example.model.Line {
  private final Client client;
  public Line(Client client, org.raml.jaxrs.example.model.Line line) {
    this.client = client;
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
  }
}
