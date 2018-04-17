package de.lmu.cis.ocrd;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class SimpleDocument implements Document {

	private String path, ocrEngine;
	private boolean isMasterOCR;
	private final TreeMap<Integer, ArrayList<String>> lines = new TreeMap<Integer, ArrayList<String>>();

	public void add(int pageno, String line) {
		if (!this.lines.containsKey(pageno)) {
			this.lines.put(pageno, new ArrayList<String>());
		}
		lines.get(pageno).add(line);
	}

	@Override
	public void eachLine(Visitor v) throws Exception {
		int pageseq = 0;
		for (Map.Entry<Integer, ArrayList<String>> e : this.lines.entrySet()) {
			assert (e.getValue() != null);
			int lineid = 0;
			for (String line : e.getValue()) {
				SimpleLine tmp = new SimpleLine().withOcr(line).withLineId(lineid).withPageId(e.getKey());
				OCRLine ocrLine = new OCRLine(ocrEngine, tmp, pageseq, isMasterOCR);
				v.visit(ocrLine);
				++lineid;
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
