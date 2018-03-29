package de.lmu.cis.ocrd;

public class OCRLine implements Comparable<OCRLine> {
	public OCRLine(String ocrEngine, Line line, int pageSeq, boolean isMasterOCR) {
		this.ocrEngine = ocrEngine;
		this.line = line;
		this.pageSeq = pageSeq;
		this.isMasterOCR = isMasterOCR;
	}

	@Override
	public String toString() {
		return this.line.toString();
	}

	@Override
	public int compareTo(OCRLine other) {
		if (this.pageSeq < other.pageSeq) {
			return -1;
		}
		if (this.pageSeq > other.pageSeq) {
			return 1;
		}
		if (this.line.getLineId() < other.line.getLineId()) {
			return -1;
		}
		if (this.line.getLineId() > other.line.getLineId()) {
			return 1;
		}
		return this.ocrEngine.compareTo(other.ocrEngine);
	}

	public final String ocrEngine;
	public final Line line;
	public final int pageSeq;
	public final boolean isMasterOCR;
}
