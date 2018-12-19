package de.lmu.cis.ocrd.test;

import de.lmu.cis.ocrd.FileTypes;
import de.lmu.cis.ocrd.Project;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class ProjectTest {
	private Project project;

	@Before
	public void init() throws Exception {
		this.project = new Project()
				.put("master-ocr", FileTypes.openDocument("src/test/resources/1841-DieGrenzboten-abbyy-small.zip"), true)
				.put("GT", FileTypes.openDocument("src/test/resources/1841-DieGrenzboten-gt-small.zip"))
				.put("other-ocr-1", FileTypes.openDocument("src/test/resources/1841-DieGrenzboten-tesseract-small.zip"));
	}

	@Test
	public void testUniqueLines() throws Exception {
		final HashMap<String, HashMap<Integer, HashSet<Integer>>> engineOfPageOfLines = new HashMap<>();
		project.eachPage((page) -> page.eachLine((line) -> {
			final String engine = line.ocrEngine;
			final int pageID = line.line.getPageId();
			final int lineID = line.line.getLineId();
			if (!engineOfPageOfLines.containsKey(line.ocrEngine)) {
				engineOfPageOfLines.put(engine, new HashMap<>());
			}
			if (!engineOfPageOfLines.get(engine).containsKey(pageID)) {
				engineOfPageOfLines.get(engine).put(pageID, new HashSet<>());
			}
			assertThat(engineOfPageOfLines.get(engine).get(pageID).contains(lineID), is(false));
			engineOfPageOfLines.get(engine).get(pageID).add(lineID);
			assertThat(engineOfPageOfLines.get(engine).get(pageID).contains(lineID), is(true));
		}));
	}
}
