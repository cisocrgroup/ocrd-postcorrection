package de.lmu.cis.ocrd.parsers.test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.TextArchiveParser;
import de.lmu.cis.ocrd.parsers.TextFileType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TextZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/1841-DieGrenzboten-gt.zip";

	public TextZipArchiveTest() throws Exception {
		setResource(resource);
		try (Archive ar = new ZipArchive(resource)) {
			setDocument(new TextArchiveParser(ar, new TextFileType()).parse());
		}
	}

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 1).getNormalized(), is("Deutschland und Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 31).getNormalized(), is("vulcanische Umänderung es in seinen Tiefen verbirgt Nur selten spaltet sich"));
	}
}