package de.lmu.cis.ocrd.ocropus.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.BaseOCRToken;
import de.lmu.cis.ocrd.ocropus.Workspace;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WorkspaceTest {
    private static final Path parametersPath = Paths.get("src/test/resources/workspace/config.json");
    private static final String ocropusDir = "src/test/resources/ocropus";
    private Workspace workspace;

    @Before
    public void init() throws IOException {
        Parameters parameters;
        try (Reader r = new FileReader(parametersPath.toFile())) {
            parameters = new Gson().fromJson(r, Parameters.class);
        }
        parameters.setOcropusOCRExtensions(Arrays.asList(".llocs.1", ".llocs.2", ".llocs.3"));
        parameters.setOcropusImageExtension(".png");
        this.workspace = new Workspace(parameters);
    }

    @Test
    public void testIsOcropus() {
        assertThat(workspace.getParameters().isOcropus(), is(true));
    }

    @Test
    public void testNumberOfBaseTokens() throws Exception {
        assertThat(workspace.getBaseOCRTokenReader(ocropusDir).read().size(), is(4));
    }

    @Test
    public void testNumberOfBaseTokensNoSubDir() throws Exception {
        assertThat(workspace.getBaseOCRTokenReader(Paths.get(ocropusDir, "0001").toString()).read().size(), is(4));
    }

    @Test
    public void testMasterOCR() throws Exception {
        final String[] want = new String[]{"ab", "cd", "ef", "ghi"};
        final List<BaseOCRToken> tokens = workspace.getBaseOCRTokenReader(ocropusDir).read();
        for (int i = 0; i < tokens.size(); i++) {
            assertThat(tokens.get(i).getMasterOCR().getWordNormalized(), is(want[i]));
        }
    }

    @Test
    public void testSecondOCR() throws Exception {
        final String[] want = new String[]{"abc", "d", "ef", "ghi"};
        final List<BaseOCRToken> tokens = workspace.getBaseOCRTokenReader(ocropusDir).read();
        for (int i = 0; i < tokens.size(); i++) {
            assertThat(tokens.get(i).getSlaveOCR(0).getWordNormalized(), is(want[i]));
        }
    }

    @Test
    public void testThirdOCR() throws Exception {
        final String[] want = new String[]{"abc", "abc", "defghi", "defghi"};
        final List<BaseOCRToken> tokens = workspace.getBaseOCRTokenReader(ocropusDir).read();
        for (int i = 0; i < tokens.size(); i++) {
            assertThat(tokens.get(i).getSlaveOCR(1).getWordNormalized(), is(want[i]));
        }
    }

    @Test
    public void testGT() throws Exception {
        final String[] want = new String[]{"abc", "d", "efghi", "efghi"};
        final List<BaseOCRToken> tokens = workspace.getBaseOCRTokenReader(ocropusDir).read();
        for (int i = 0; i < tokens.size(); i++) {
            assertThat(tokens.get(i).getGT().orElse(""), is(want[i]));
        }
    }

    @Test
    public void testIds() throws Exception {
        final int[] ids = new int[]{1, 2, 3, 4};
        final List<BaseOCRToken> tokens = workspace.getBaseOCRTokenReader(ocropusDir).read();
        for (int i = 0; i < tokens.size(); i++) {
            assertThat(tokens.get(i).getID(), is(Integer.toString(ids[i])));
        }
    }
}
