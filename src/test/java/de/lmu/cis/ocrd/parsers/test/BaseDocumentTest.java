package de.lmu.cis.ocrd.parsers.test;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.SimpleDocument;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class BaseDocumentTest {
	private Document doc;
	private Line line;
	private String resource;

	@Test
	public void checkResource() {
		if (this.doc instanceof SimpleDocument) {
			assertThat(this.resource, is(((SimpleDocument) doc).getPath()));
		}
	}

	Line findLine(int pageno, int lineno) throws Exception {
		this.doc.eachLine((t) -> {
			// System.out.println("PAGEID: " + t.line.getPageId() + " LINEID: " + t.line.getLineId());
			if (t.line.getPageId() == pageno && t.line.getLineId() == lineno) {
				line = t.line;
			}
		});
		if (this.line == null) {
			throw new Exception("cannot find line: " + pageno + ", line: " + lineno);
		}
		return this.line;
	}

	Document getDocument() {
		return doc;
	}

	void setDocument(Document doc) {
		this.doc = doc;
	}

	protected void setResource(String resource) {
		this.resource = resource;
	}
}
