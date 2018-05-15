package de.lmu.cis.ocrd.test;
import de.lmu.cis.ocrd.FileTypes;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GuessFileTypeTest {

	@Test
	public void testABBYY() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-abbyy.zip";
		final FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.ABBYY_XML));
	}

	@Test
	public void testHOCR() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-tesseract.zip";
		final FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.HOCR));
	}

	@Test
	public void testOcropus() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-ocropus.zip";
		final FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.OCROPUS_LLOCS));
	}

	@Test
	public void testOcropusNoLlocs() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-ocropus-no-llocs.zip";
		final FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.OCROPUS));
	}

	@Test
	public void testOcropusGT() throws Exception {
		final String resource = "src/test/resources/1841-DieGrenzboten-ocropus-gt.zip";
		final FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.OCROPUS_GT));
	}

	@Test
    public void testText() throws Exception {
        final String resource = "src/test/resources/1841-DieGrenzboten-gt.zip";
        final FileTypes.Type type = FileTypes.guess(resource);
        assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
        assertThat(type.getOCRType(), is(FileTypes.OCRType.TEXT));
    }


	@Test
	public void testPageXML() throws Exception {
		final String resource = "src/test/resources/euler_rechenkunst01_1738.zip";
		FileTypes.Type type = FileTypes.guess(resource);
		assertThat(type.getArchiveType(), is(FileTypes.ArchiveType.ZIP));
		assertThat(type.getOCRType(), is(FileTypes.OCRType.PAGE_XML));
	}
}
