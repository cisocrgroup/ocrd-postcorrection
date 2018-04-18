import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.zip.ZipFile;

import org.junit.Test;

import de.lmu.cis.ocrd.parsers.HOCRFileType;
import de.lmu.cis.ocrd.parsers.HOCRParserFactory;
import de.lmu.cis.ocrd.parsers.ZipParser;

public class HOCRZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/1841-DieGrenzboten-tesseract.zip";

	public HOCRZipArchiveTest() throws Exception {
		setResource(resource);
		try (ZipFile zip = new ZipFile(resource)) {
			setDocument(new ZipParser(new HOCRParserFactory(), new HOCRFileType(), zip).parse());
		}
	}

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 1).getNormalized(), is("Deutschland und Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 0x1F).getNormalized(), is("4"));
	}
}