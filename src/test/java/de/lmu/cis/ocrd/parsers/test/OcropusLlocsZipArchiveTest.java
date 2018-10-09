package de.lmu.cis.ocrd.parsers.test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.OcropusArchiveLlocsParser;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class OcropusLlocsZipArchiveTest extends BaseDocumentTest {

	private static final String resource = "src/test/resources/1841-DieGrenzboten-ocropus.zip";

	public OcropusLlocsZipArchiveTest() throws Exception {
		setResource(resource);
		try (Archive ar = new ZipArchive(resource)) {
			setDocument(new OcropusArchiveLlocsParser(ar).parse());
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
