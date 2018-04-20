package de.lmu.cis.ocrd.parser.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.ArchiveParser;
import de.lmu.cis.ocrd.parsers.HOCRFileType;
import de.lmu.cis.ocrd.parsers.HOCRParserFactory;

public class HOCRZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/1841-DieGrenzboten-tesseract.zip";

	public HOCRZipArchiveTest() throws Exception {
		setResource(resource);
		try (Archive ar = new ZipArchive(resource)) {
			setDocument(new ArchiveParser(new HOCRParserFactory(), new HOCRFileType(), ar).parse());
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