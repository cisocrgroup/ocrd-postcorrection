import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.zip.ZipFile;

import org.junit.Test;

import de.lmu.cis.ocrd.parsers.PageXMLFileType;
import de.lmu.cis.ocrd.parsers.PageXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ZipParser;

public class PageXMLZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/euler_rechenkunst01_1738.zip";

	public PageXMLZipArchiveTest() throws Exception {
		setResource(resource);
		try (ZipFile zip = new ZipFile(resource)) {
			setDocument(new ZipParser(new PageXMLParserFactory(), new PageXMLFileType(), zip).parse());
		}
	}

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(24, 2).getNormalized(), is("Hiebey iſt nun zu mercken daß das Zeichen"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(41, 29).getNormalized(), is("Wir"));
	}
}