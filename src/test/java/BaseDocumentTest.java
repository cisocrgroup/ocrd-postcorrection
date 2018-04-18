import org.junit.Before;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.parsers.ArchiveFactory;

public class BaseDocumentTest {
	private Document doc;
	private Line line;
	private final ArchiveFactory factory;

	public BaseDocumentTest(ArchiveFactory factory) {
		this.factory = factory;
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

	@Before
	public void readArchive() throws Exception {
		this.doc = this.factory.create();
	}
}
