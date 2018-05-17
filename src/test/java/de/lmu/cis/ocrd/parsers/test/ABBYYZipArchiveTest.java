package de.lmu.cis.ocrd.parsers.test;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.ABBYYXMLFileType;
import de.lmu.cis.ocrd.parsers.ABBYYXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ArchiveParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

	@Test
    public void testUniqueLineIDs() throws Exception {
	    final HashMap<Integer, HashSet<Integer>> ids = new HashMap<>();
	    getDocument().eachLine((line)->{
	        final int pageID = line.line.getPageId();
            final int lineID = line.line.getLineId();
            if (!ids.containsKey(pageID)) {
                ids.put(pageID, new HashSet<>());
            }
            assertThat(ids.get(pageID).contains(lineID), is(false));
            ids.get(pageID).add(lineID);
        });
    }
}
