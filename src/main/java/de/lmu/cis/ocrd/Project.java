package de.lmu.cis.ocrd;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Project implements Document {

	private final HashMap<String, Entry> documents = new HashMap<String, Entry>();
	private final TreeMap<Integer, Page> pages = new TreeMap<>();

	@Override
	public void eachLine(Visitor v) throws Exception {
		for (Map.Entry<String, Entry> entry : this.documents.entrySet()) {
			entry.getValue().document.eachLine((line) -> {
				v.visit(line);
			});
		}
	}

	public void eachPage(PageVisitor v) throws Exception {
		for (Map.Entry<Integer, Page> entry : pages.entrySet()) {
			v.visit(entry.getValue());
		}
	}

	public Project put(String ocr, Document document) throws Exception {
		return this.put(ocr, document, false);
	}

	public Project put(String ocr, Document document, boolean isMasterOCR) throws Exception {
		this.documents.put(ocr, new Entry(document, isMasterOCR));
		document.eachLine((line) -> {
			if (!this.pages.containsKey(line.pageSeq)) {
				this.pages.put(line.pageSeq, new Page(line.pageSeq));
			}
			line.isMasterOCR = isMasterOCR;
			line.ocrEngine = ocr;
			this.pages.get(line.pageSeq).add(line);
		});
		return this;
	}

	public interface PageVisitor {
		void visit(Page page) throws Exception;
	}

	private static class Entry {
		final Document document;
		final boolean isMasterOCR;

		Entry(Document document, boolean isMasterOCR) {
			this.document = document;
			this.isMasterOCR = isMasterOCR;
		}
	}
}
