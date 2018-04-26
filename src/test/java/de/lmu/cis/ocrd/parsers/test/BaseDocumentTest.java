package de.lmu.cis.ocrd.parsers.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.SimpleDocument;

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

	protected Line findLine(int pageno, int lineno) throws Exception {
		this.doc.eachLine(new Document.Visitor() {
			@Override
			public void visit(OCRLine t) throws Exception {
				// Logger.getLogger("findLine").log(Level.INFO,
				// "pageid: " + t.line.getPageId() + ", lineid: " + t.line.getLineId());
				if (t.line.getPageId() == pageno && t.line.getLineId() == lineno) {
					line = t.line;
				}
			}
		});
		if (this.line == null) {
			throw new Exception("cannot find line: " + pageno + ", line: " + lineno);
		}
		return this.line;
	}

	protected void setDocument(Document doc) {
		this.doc = doc;
	}

	protected void setResource(String resource) {
		this.resource = resource;
	}
}
