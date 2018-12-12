package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.Page;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class METSTest {
	private final static String imgGrpID = "OCR-D-IMG" +
			"-aventinus_grammatica_1515";
	private METS mets;

	@Before
	public void init() throws Exception {
		mets = METS.open(Paths.get("src/test/resources/mets.xml"));
	}

	@Test
	public void testFindFileGrpFiles() {
		assertThat(mets.findFileGrpFiles(imgGrpID).size(), is(3));
	}

	@Test
	public void testDoNotFindAnyFileGrpFiles() {
		assertThat(mets.findFileGrpFiles("INVALID").size(), is(0));
	}

	@Test
	public void testFileID() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getID(), is("OCR-D" +
				"-IMG-aventinus_grammatica_1515-aventinus_grammatica_1515_0007"));
	}

	@Test
	public void testFileMIMEType() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getMIMEType(), is(
				"image/tif"));
	}

	@Test
	public void testFileGroupID() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getGroupID(),
				is(imgGrpID));
	}

	@Test
	public void testFilePath() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getFLocat(),
				is("file://workspace/OCR-D-IMG" +
						"-aventinus_grammatica_1515" +
						"/aventinus_grammatica_1515_0007.tif"));
	}

	@Test
	public void testOpenPageXML() throws Exception {
		List<METS.File> files = mets.findFileGrpFiles("OCR-D-TEST");
		assertThat(files.size(), is(1));
		assertThat(files.get(0).getMIMEType(), is(Page.MIMEType));
		final Page page = Page.parse(files.get(0).open());
		assertThat(page, notNullValue());
	}
}
