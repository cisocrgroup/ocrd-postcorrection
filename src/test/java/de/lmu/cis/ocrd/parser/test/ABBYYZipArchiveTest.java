package de.lmu.cis.ocrd.parser.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.ABBYYXMLFileType;
import de.lmu.cis.ocrd.parsers.ABBYYXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ArchiveParser;

public class ABBYYZipArchiveTest extends BaseDocumentTest {
	private static final String resource = "src/test/resources/1841-DieGrenzboten-abbyy.zip";

	public ABBYYZipArchiveTest() throws Exception {
		setResource(resource);
		try (Archive ar = new ZipArchive(resource)) {
			setDocument(new ArchiveParser(new ABBYYXMLParserFactory(), new ABBYYXMLFileType(), ar).parse());
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
