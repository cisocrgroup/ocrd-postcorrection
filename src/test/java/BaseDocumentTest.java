import java.io.IOException;

import de.lmu.cis.ocrd.Archive;
import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.OCRLine;

class BaseDocumentTest {
	private Document doc;
	private Line line;

	protected Line findLine(int pageno, int lineno) throws Exception {
		this.doc.eachLine(new Document.Visitor() {
			@Override
			public void visit(OCRLine t) throws Exception {
				if (t.line.getPageId() == pageno && t.line.getLineId() == lineno) {
					line = t.line;
				}
			}
		});
		if (this.line == null) {
			throw new Exception("cannot find line");
		}
		return this.line;
	}

	protected void readArchive(String ar) throws IOException {
		this.doc = Archive.createOcropusDocumentFromZipArchive(ar);
	}
}
