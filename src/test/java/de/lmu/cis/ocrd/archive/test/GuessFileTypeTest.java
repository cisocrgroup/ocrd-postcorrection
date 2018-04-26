package de.lmu.cis.ocrd.archive.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.DirectoryArchive;
import de.lmu.cis.ocrd.archive.Entry;
import de.lmu.cis.ocrd.parser.test.BaseDocumentTest;
import de.lmu.cis.ocrd.parsers.FileTypes;

public class GuessFileTypeTest extends BaseDocumentTest {
	
	@Test
	public void testABBYY() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-abbyy.zip";
		FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.ABBYY));
	}
}
