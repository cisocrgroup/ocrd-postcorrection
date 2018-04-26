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

	@Test
	public void testHOCR() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-tesseract.zip";
		FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.HOCR));
	}

	@Test
	public void testOcropus() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-ocropus.zip";
		FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.OCROPUS));
	}

	@Test
	public void testPageXML() throws Exception {
		final String resource = "src/test/resources/euler_rechenkunst01_1738.zip";
		FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.PAGEXML));
	}
}
