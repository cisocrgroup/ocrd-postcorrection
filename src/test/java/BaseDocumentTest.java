import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.OCRLine;

public class BaseDocumentTest {
	private Document doc;
	private Line line;

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
}
