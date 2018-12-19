package de.lmu.cis.ocrd;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class SimpleDocument implements Document {

	private final TreeMap<Integer, ArrayList<Line>> lines = new TreeMap<>();
	private String path, ocrEngine;
	private boolean isMasterOCR;

	public void add(int pageID, Line line) {
		if (!this.lines.containsKey(pageID)) {
			this.lines.put(pageID, new ArrayList<>());
		}
		lines.get(pageID).add(line);
	}

	public void add(SimpleDocument o) {
		for (Map.Entry<Integer, ArrayList<Line>> e : o.lines.entrySet()) {
			this.lines.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void eachLine(Visitor v) throws Exception {
		int pageseq = 0;
		for (Map.Entry<Integer, ArrayList<Line>> e : this.lines.entrySet()) {
			assert (e.getValue() != null);
			for (Line line : e.getValue()) {
				OCRLine ocrLine = new OCRLine(ocrEngine, line, pageseq, isMasterOCR);
				v.visit(ocrLine);
			}
			++pageseq;
		}
	}

	public String getPath() {
		return this.path;
	}

	public SimpleDocument withIsMasterOcr(boolean mocr) {
		this.isMasterOCR = mocr;
		return this;
	}

	public SimpleDocument withOcrEngine(String engine) {
		this.ocrEngine = engine;
		return this;
	}

	public SimpleDocument withPath(String path) {
		this.path = path;
		return this;
	}
}
