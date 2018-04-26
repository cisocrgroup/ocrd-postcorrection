package de.lmu.cis.ocrd.parsers.test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.parsers.FileTypes;

public class GuessFileTypeTest {
	
	@Test
	public void testABBYY() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-abbyy.zip";
		FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.ABBYY));
	}
}
