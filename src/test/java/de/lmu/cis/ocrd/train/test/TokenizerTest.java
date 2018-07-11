package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.train.Environment;
import de.lmu.cis.ocrd.train.Tokenizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TokenizerTest extends TestBase {
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
    public void testUniqueIDs() throws Exception {
        final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> pageOfLineOfToken = new HashMap<>();
        tokenizer.eachToken((token)->{
            final int pageID = token.getMasterOCR().getLine().getPageId();
            final int lineID = token.getMasterOCR().getLine().getLineId();
            final int tokenID = token.getID();
            if (!pageOfLineOfToken.containsKey(pageID)) {
                pageOfLineOfToken.put(pageID, new HashMap<>());
            }
            if (!pageOfLineOfToken.get(pageID).containsKey(lineID)) {
                pageOfLineOfToken.get(pageID).put(lineID, new HashSet<>());
            }
            assertThat(pageOfLineOfToken.get(pageID).get(lineID).contains(tokenID), is(false));
            pageOfLineOfToken.get(pageID).get(lineID).add(tokenID);
            assertThat(pageOfLineOfToken.get(pageID).get(lineID).contains(tokenID), is(true));
        });
    }

    @After
    public void deInit() throws IOException {
        environment.remove();
    }
}
