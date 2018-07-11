package de.lmu.cis.ocrd.parsers.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.ALTOXMLFileType;
import de.lmu.cis.ocrd.parsers.ALTOXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ArchiveParser;

public class ALTOZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/euler_rechenkunst01_1738.zip";

	public ALTOZipArchiveTest() throws Exception {
		setResource(resource);
		try (Archive ar = new ZipArchive(resource)) {
			setDocument(new ArchiveParser(new ALTOXMLParserFactory(), new ALTOXMLFileType(), ar).parse());
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