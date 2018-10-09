package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.train.Environment;
import de.lmu.cis.ocrd.train.Tokenizer;
import de.lmu.cis.ocrd.train.TrainSetSplitter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;

public class TrainSetSplitterTest extends TestBase {
	private Environment environment;
	private Tokenizer tokenizer;

	@Before
	public void init() throws IOException {
		final String resources = "src/test/resources";
		environment = newEnvironment()
				.withCopyTrainingFiles(false)
				.withGT(resources + "/1841-DieGrenzboten-gt-small.zip")
				.withMasterOCR(resources + "/1841-DieGrenzboten-abbyy-small.zip")
				.addOtherOCR(resources + "/1841-DieGrenzboten-tesseract-small.zip");
		tokenizer = new Tokenizer(environment);
	}

	@Test
	public void testTrainSplitWithZero() throws Exception {
		final HashMap<Integer, Boolean> pages = new HashMap<>();
		new TrainSetSplitter(new Tokenizer(environment), 0).eachToken((Token t, boolean isTrain) -> {
			final int pageID = t.getMasterOCR().getLine().getPageId();
			pages.put(pageID, isTrain);
		});
		assertThat(pages.size(), is(2));
		assertThat(pages.get(179392), is(true));
		assertThat(pages.get(179393), is(true));
	}

	@Test
	public void testTrainSplit() throws Exception {
		// two page ids: 179392 and 179393
		// - 179392 is training
		// - 179393 is evaluation
		final HashMap<Integer, Boolean> pages = new HashMap<>();
		new TrainSetSplitter(new Tokenizer(environment), 2).eachToken((Token t, boolean isTrain) -> {
			final int pageID = t.getMasterOCR().getLine().getPageId();
			pages.put(pageID, isTrain);
		});
		assertThat(pages.size(), is(2));
		assertThat(pages.get(179392), is(true));
		assertThat(pages.get(179393), is(false));
	}

	@Test
	public void testLineOrdering() throws Exception {
		final HashMap<Integer, ArrayList<Integer>> pageLines = new HashMap<>();
		new TrainSetSplitter(new Tokenizer(environment), 2).eachToken((Token t, boolean isTrain) -> {
			final int pageID = t.getMasterOCR().getLine().getPageId();
			final int lineID = t.getMasterOCR().getLine().getPageId();
			if (!pageLines.containsKey(pageID)) {
				pageLines.put(pageID, new ArrayList<>());
			}
			pageLines.get(pageID).add(lineID);
		});
		assertThat(pageLines.size(), is(2));
		for (ArrayList<Integer> lineIDs : pageLines.values()) {
			int last = -1; // lineIDs start with 1
			for (Integer id : lineIDs) {
				// multiple lines with the same line ID are possible
				assertThat(last <= id, is(true));
				last = id;
			}
		}
	}

	@Test
	public void testTokenOrdering() throws Exception {
		final HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> pageLineTokens = new HashMap<>();
		new TrainSetSplitter(new Tokenizer(environment), 2).eachToken((Token t, boolean isTrain) -> {
			final int pageID = t.getMasterOCR().getLine().getPageId();
			final int lineID = t.getMasterOCR().getLine().getLineId();
			if (!pageLineTokens.containsKey(pageID)) {
				pageLineTokens.put(pageID, new HashMap<>());
			}
			if (!pageLineTokens.get(pageID).containsKey(lineID)) {
				pageLineTokens.get(pageID).put(lineID, new ArrayList<>());
			}
			pageLineTokens.get(pageID).get(lineID).add(t.getID());
//            System.out.println(pageID + " " + lineID + " " + t.getID());
//            System.out.println(t.getMasterOCR().getLine().getNormalized());
//            System.out.println(t.getOtherOCRAt(0).getLine().getNormalized());
//            System.out.println(t.getOtherOCRAt(0).getLine().getLineId());
//            System.out.println(t.getMasterOCR());
		});
		for (Map.Entry<Integer, HashMap<Integer, ArrayList<Integer>>> lineTokens : pageLineTokens.entrySet()) {
			for (Map.Entry<Integer, ArrayList<Integer>> tokenIDs : lineTokens.getValue().entrySet()) {
				int last = -1; // tokenIDs start with 1
				for (Integer id : tokenIDs.getValue()) {
//                    System.out.println("pageID: " + lineTokens.getKey() + ", lineID: " + tokenIDs.getKey());
//                    System.out.println("last: " + last + ", id: " + id);
					// multiple tokens with the same ID are *not* possible
					assertThat(last < id, is(true));
					last = id;
				}
			}
		}
	}

	@After
	public void deInit() throws IOException {
		environment.remove();
	}
}
