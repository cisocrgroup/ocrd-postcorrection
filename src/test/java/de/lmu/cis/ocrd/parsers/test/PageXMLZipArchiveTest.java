package de.lmu.cis.ocrd.parsers.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.ArchiveParser;
import de.lmu.cis.ocrd.parsers.PageXMLFileType;
import de.lmu.cis.ocrd.parsers.PageXMLParserFactory;

public class PageXMLZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/euler_rechenkunst01_1738.zip";

	public PageXMLZipArchiveTest() throws Exception {
		setResource(resource);
		try (Archive ar = new ZipArchive(resource)) {
			setDocument(new ArchiveParser(new PageXMLParserFactory(), new PageXMLFileType(), ar).parse());
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