package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.Page;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class METSTest {
	private final static String imgGrpID = "OCR-D-IMG-aventinus_grammatica_1515";
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
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getID(), is("OCR-D-IMG-aventinus_grammatica_1515-aventinus_grammatica_1515_0007"));
	}

	@Test
	public void testFileMIMEType() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getMIMEType(), is("image/tif"));

	}

	@Test
	public void testFileGroupID() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getGroupID(), is(imgGrpID));
	}

	@Test
	public void testFilePath() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getFLocat(),
				is("file://workspace/OCR-D-IMG-aventinus_grammatica_1515/aventinus_grammatica_1515_0007.tif"));
	}

	@Test
	public void testLocType() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getLocType(), is("OTHER"));
	}

	@Test
	public void testOtherLocType() {
		assertThat(mets.findFileGrpFiles(imgGrpID).get(0).getOtherLocType(), is("FILE"));
	}

	@Test
	public void testAddFPtr() {
		final METS.FPtr fptr = mets.addFPtr(mets.findFileGrpFiles(imgGrpID).get(0)).withFileID("testFileID");
		assertThat(fptr.getFileID(), is("testFileID"));
	}

	@Test
	public void testOpenPageXML() throws Exception {
		List<METS.File> files = mets.findFileGrpFiles("OCR-D-TEST");
		assertThat(files.size(), is(1));
		assertThat(files.get(0).getMIMEType(), is(Page.MIMEType));
		final Page page = Page.parse(Paths.get(files.get(0).getFLocat()), files.get(0).openInputStream());
		assertThat(page, notNullValue());
	}

	// XPATH does not seem to work on newly inserted nodes?
	@Test
	public void testAddFileToFileGrpMIMEType() throws Exception {
		METS.File file = mets.addFileToFileGrp("new-file-grp").withMIMEType("test/mimeType");
		assertThat(file.getMIMEType(), is("test/mimeType"));
		withTmpMETSFile((mets)->{
		    assertThat(mets.findFileGrpFiles("new-file-grp").size(), is(1));
		    assertThat(mets.findFileGrpFiles("new-file-grp").get(0).getMIMEType(), is("test/mimeType"));
		});
	}

	@Test
	public void testAddFileToFileGrpFLocat() throws Exception {
		mets.addFileToFileGrp("new-file-grp").withFLocat("test-flocat");
		withTmpMETSFile((mets)-> {
			assertThat(mets.findFileGrpFiles("new-file-grp").size(), is(1));
			assertThat(mets.findFileGrpFiles("new-file-grp").get(0).getFLocat(), is("test-flocat"));
		});
	}

	@Test
	public void testAddFileToFileGrpGroupID() throws Exception {
		METS.File file = mets.addFileToFileGrp("new-file-grp").withGroupID("test-gid");
		assertThat(file.getGroupID(), is("test-gid"));
		withTmpMETSFile((mets)-> {
			assertThat(mets.findFileGrpFiles("new-file-grp").size(), is(1));
			assertThat(mets.findFileGrpFiles("new-file-grp").get(0).getGroupID(), is("test-gid"));
		});
	}

	@Test
	public void testAddFileToFileGrpID() throws Exception {
		METS.File file = mets.addFileToFileGrp("new-file-grp").withID("test-id");
		assertThat(file.getID(), is("test-id"));
		withTmpMETSFile((mets)-> {
			assertThat(mets.findFileGrpFiles("new-file-grp").size(), is(1));
			assertThat(mets.findFileGrpFiles("new-file-grp").get(0).getID(), is("test-id"));
		});
	}

	private interface Lambda {
		void execute(METS mets) throws MalformedURLException;
	}

	private void withTmpMETSFile(Lambda l) throws Exception {
		File file = File.createTempFile("mets", ".xml");
		file.deleteOnExit();
		mets.save(file);
		mets = METS.open(file);
		l.execute(mets);
	}
}
