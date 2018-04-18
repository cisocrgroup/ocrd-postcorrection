import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.zip.ZipFile;

import org.junit.Test;

import de.lmu.cis.ocrd.parsers.OcropusZipParser;

public class OcropusZipArchiveTest extends BaseDocumentTest {

	private static final String resource = "src/test/resources/1841-DieGrenzboten-ocropus.zip";

	public OcropusZipArchiveTest() throws Exception {
		try (ZipFile zip = new ZipFile(resource)) {
			setDocument(new OcropusZipParser(zip).parse());
		}
	}

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 1).getNormalized(), is("Deuiſchland und Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 0x1F).getNormalized(), is("a4"));
	}
}
