import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.zip.ZipFile;

import org.junit.Test;

import de.lmu.cis.ocrd.parsers.ABBYYXMLFileType;
import de.lmu.cis.ocrd.parsers.ABBYYXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ZipParser;

public class ABBYYZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/1841-DieGrenzboten-abbyy.zip";

	public ABBYYZipArchiveTest() throws Exception {
		try (ZipFile zip = new ZipFile(resource)) {
			setDocument(new ZipParser(new ABBYYXMLParserFactory(), new ABBYYXMLFileType(), zip).parse());
		}
	}

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 1).getNormalized(), is("Dmlschland md Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 0x1F).getNormalized(), is("14"));
	}
}