package de.lmu.cis.ocrd;

import java.util.HashMap;
import java.util.Map;

public class Project implements Document {

	private static class Entry {
		final Document document;
		final boolean isMasterOCR;

		Entry(Document document, boolean isMasterOCR) {
			this.document = document;
			this.isMasterOCR = isMasterOCR;
		}
	}

	private final HashMap<String, Entry> documents = new HashMap<String, Entry>();

	@Override
	public void eachLine(Visitor v) throws Exception {
		for (Map.Entry<String, Entry> entry : this.documents.entrySet()) {
			entry.getValue().document.eachLine((line) -> {
				line.ocrEngine = entry.getKey();
				line.isMasterOCR = entry.getValue().isMasterOCR;
				v.visit(line);
			});
		}
	}

	public Project put(String ocr, Document document) {
		return this.put(ocr, document, false);
	}

	public Project put(String ocr, Document document, boolean isMasterOCR) {
		this.documents.put(ocr, new Entry(document, isMasterOCR));
		return this;
	}
}
