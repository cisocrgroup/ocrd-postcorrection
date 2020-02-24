package de.lmu.cis.ocrd.ocropus.test;

import de.lmu.cis.ocrd.ocropus.BaseOCRToken;
import de.lmu.cis.ocrd.ocropus.TSVLineAlignment;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TSVLineAlignmentTest {
    private Path img = Paths.get("src/test/resources/ocropus/0001/0001.bin.png");
    private final List<String> extensions = Arrays.asList(".llocs.1", ".llocs.2", ".llocs.3");
    private TSVLineAlignment alignment;

    @Before
    public void init() {
        alignment = new TSVLineAlignment(img);
    }

    @Test
    public void testReadTokens() throws Exception {
        final List<BaseOCRToken> tokens = alignment.align(extensions);
        assertThat(tokens.size(), is(3));
    }

    @Test
    public void testMasterOCRNormLine() throws Exception {
        final List<BaseOCRToken> tokens = alignment.align(extensions);
        for (BaseOCRToken token: tokens) {
            assertThat(token.getMasterOCR().getLineNormalized(), is("ab cd ef"));
        }
    }

    @Test
    public void testFirstSlaveOCRNormLine() throws Exception {
        final List<BaseOCRToken> tokens = alignment.align(extensions);
        for (BaseOCRToken token: tokens) {
            assertThat(token.getSlaveOCR(0).getLineNormalized(), is("abc d ef"));
        }
    }

    @Test
    public void testSecondSlaveOCRNormLine() throws Exception {
        final List<BaseOCRToken> tokens = alignment.align(extensions);
        for (BaseOCRToken token: tokens) {
            assertThat(token.getSlaveOCR(1).getLineNormalized(), is("abc def"));
        }
    }

    @Test
    public void testMasterOCRHasGT() throws Exception {
        final List<BaseOCRToken> tokens = alignment.align(extensions);
        for (BaseOCRToken token: tokens) {
            assertThat(token.getGT().isPresent(), is(true));
        }
    }
}
