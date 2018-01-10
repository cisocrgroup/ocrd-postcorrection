package de.lmu.cis.pocoweb;

class LineData {
  public String imgFile;
  public String cor;
  public String ocr;
  public int[] cuts;
  public double[] confidences;
  public int lineId;
  public int pageId;
  public int projectId;
  public double averageConfidence;
  public boolean isFullyCorrected;
  public boolean isPartiallyCorrected;
  public BoxData box;
}
