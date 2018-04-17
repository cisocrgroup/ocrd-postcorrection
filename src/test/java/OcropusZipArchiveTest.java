import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.lmu.cis.ocrd.Archive;
import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.OCRLine;

public class OcropusZipArchiveTest {
	private Document doc;
	private Line line;

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 0).getNormalized(), is("Deuiſchland und Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 0x1F - 1).getNormalized(), is("a4"));
	}

	private Line findLine(int pageno, int lineno) throws Exception {
		doc.eachLine(new Document.Visitor() {
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

	@Before
	public void readOcropusZipArchive() throws IOException {
		doc = Archive.createOcropusDocumentFromZipArchive("src/test/resources/1841-DieGrenzboten-ocropus.zip");
	}
}
