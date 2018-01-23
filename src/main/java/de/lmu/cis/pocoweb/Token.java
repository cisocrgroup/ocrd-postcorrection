package de.lmu.cis.pocoweb;

import org.raml.jaxrs.example.model.Box;

public class Token {
  protected int projectId;
  protected int pageId;
  protected int lineId;
  protected int offset;
  protected int tokenId;
  protected boolean isFullyCorrected;
  protected boolean isPartiallyCorrected;
  protected String ocr;
  protected String cor;
  protected float averageConfidence;
  protected Box box;
  protected boolean isNormal;

  public int getProjectId() { return this.projectId; }
  public void setProjectId(int value) { this.projectId = value; }
  public Token withProjectId(int value) {
    this.projectId = value;
    return this;
  }
  public int getPageId() { return this.pageId; }
  public void setPageId(int value) { this.pageId = value; }
  public Token withPageId(int value) {
    this.pageId = value;
    return this;
  }
  public int getLineId() { return this.lineId; }
  public void setLineId(int value) { this.lineId = value; }
  public Token withLineId(int value) {
    this.lineId = value;
    return this;
  }
  public int getTokenId() { return this.tokenId; }
  public void setTokenId(int value) { this.tokenId = value; }
  public Token withTokenId(int value) {
    this.tokenId = value;
    return this;
  }
  public int getOffset() { return this.offset; }
  public void setOffset(int value) { this.offset = value; }
  public Token withOffset(int value) {
    this.offset = value;
    return this;
  }
  public boolean getIsFullyCorrected() { return this.isFullyCorrected; }
  public void setIsFullyCorrected(boolean value) {
    this.isFullyCorrected = value;
  }
  public Token withIsFullyCorrected(boolean value) {
    this.isFullyCorrected = value;
    return this;
  }
  public boolean getIsPartiallyCorrected() { return this.isPartiallyCorrected; }
  public void setIsPartiallyCorrected(boolean value) {
    this.isPartiallyCorrected = value;
  }
  public Token withIsPartiallyCorrected(boolean value) {
    this.isPartiallyCorrected = value;
    return this;
  }
  public String getOcr() { return this.ocr; }
  public void setOcr(String value) { this.ocr = value; }
  public Token withOcr(String value) {
    this.ocr = value;
    return this;
  }
  public String getCor() { return this.cor; }
  public void setCor(String value) { this.cor = value; }
  public Token withCor(String value) {
    this.cor = value;
    return this;
  }
  public float getAverageConfidence() { return this.averageConfidence; }
  public void setAverageConfidence(float value) {
    this.averageConfidence = value;
  }
  public Token withAverageConfidence(float value) {
    this.averageConfidence = value;
    return this;
  }
  public Box getBox() { return this.box; }
  public void setBox(Box value) { this.box = value; }
  public Token withBox(Box value) {
    this.box = value;
    return this;
  }
  public boolean getIsNormal() { return this.isNormal; }
  public void setIsNormal(boolean value) { this.isNormal = value; }
  public Token withIsNormal(boolean value) {
    this.isNormal = value;
    return this;
  }
}
