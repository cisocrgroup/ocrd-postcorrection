package de.lmu.cis.ocrd.calamari.test;

import de.lmu.cis.ocrd.calamari.Workspace;
import de.lmu.cis.ocrd.config.Parameters;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WorkspaceTest {
    private Workspace ws;
    private final static String dir = "src/test/resources";

    @Before
    public void init() {
        Parameters parameters = new Parameters();
        parameters.setMaxTokens(50);
        ws = new Workspace(parameters);
    }

    @Test
    public void testFirstTokenGT() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getGT().orElse(""), is("Weſer"));
    }

    @Test
    public void testFirstTokenPrimaryOCR() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getMasterOCR().getWordNormalized(), is("Weſer"));
    }

    @Test
    public void testFirstTokenSecondaryOCR() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getSlaveOCR(1).getWordNormalized(), is("Weſer"));
    }

    @Test
    public void testFirstTokenPrimaryOCRCharConf() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getMasterOCR().getCharacterConfidenceAt(0), is(.821114924028022));
    }

    @Test
    public void testFirstTokenPrimaryConf() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getMasterOCR().getConfidence(), is(.909038138862948));
    }

    @Test
    public void testFirstTokenSecondaryConf() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getSlaveOCR(1).getConfidence(), is(.997808));
    }

    @Test
    public void testFirstTokenPrimaryLine() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getMasterOCR().getLineNormalized(), is("Weſer ſee gelegen wirdeſonſt et"));
    }

    @Test
    public void testFirstTokenSecondaryLine() throws Exception {
        assertThat(ws.getBaseOCRTokenReader(dir).read().get(0).getSlaveOCR(1).getLineNormalized(), is("Weſer ſee gelegen wirdt ſonſt et"));
    }
}
