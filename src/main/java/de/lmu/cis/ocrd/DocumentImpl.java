package de.lmu.cis.ocrd;

import java.util.ArrayList;

public class DocumentImpl implements Document {

	private String path, ocrEngine;
	private boolean isMasterOCR;
	private final ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();

	public DocumentImpl withPath(String path) {
		this.path = path;
		return this;
	}

	public String getPath() {
		return this.path;
	}

	public DocumentImpl withIsMasterOcr(boolean mocr) {
		this.isMasterOCR = mocr;
		return this;
	}

	public DocumentImpl withOcrEngine(String engine) {
		this.ocrEngine = engine;
		return this;
	}

	public void add(int pageno, String line) {
		while (lines.size() <= pageno) {
			lines.add(null);
		}
		if (lines.get(pageno) == null) {
			lines.set(pageno, new ArrayList<String>());
		}
		lines.get(pageno).add(line);
	}

	@Override
	public void eachLine(Visitor v) throws Exception {
		int pageseq = 0;
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i) == null) {
				continue;
			}
			for (int j = 0; j < lines.get(i).size(); j++) {
				LineImpl line = new LineImpl().withOcr(lines.get(i).get(j)).withLineId(j).withPageId(i);
				OCRLine ocrLine = new OCRLine(ocrEngine, line, pageseq, isMasterOCR);
				v.visit(ocrLine);
			}
			pageseq++;
		}
	}

}
