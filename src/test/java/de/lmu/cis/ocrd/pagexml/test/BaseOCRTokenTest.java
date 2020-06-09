package de.lmu.cis.ocrd.pagexml.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.pagexml.BaseOCRToken;
import de.lmu.cis.ocrd.pagexml.Workspace;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BaseOCRTokenTest {
    private Workspace workspace;

    @Before
    public void init() throws Exception {
        Parameters parameters;
        try(Reader r = new FileReader("src/test/resources/workspace/config.json")) {
            parameters = new Gson().fromJson(r, Parameters.class);
        }
        workspace = new Workspace(Paths.get("src/test/resources/workspace/mets.xml"), parameters);
    }

    @Test
    public void testNormalizedWords() throws Exception {
        assertThat(getToken(1).getMasterOCR().getWordNormalized(), is("Seneribus"));
        assertThat(getToken(2).getMasterOCR().getWordNormalized(), is("nominum"));
    }

    @Test
    public void testCorrection() throws Exception {
        getToken(1).correct("correction", 0.8, true);
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getIndex(), is(1));
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getConfidence(), is(0.8));
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getDataType(), is("OCR-D-CIS-POST-CORRECTION"));
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getDataTypeDetails(), is("master-ocr: Seneribus"));
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getUnicode(), is("Correction")); // case handling
    }

    @Test
    public void testNotCorrection() throws Exception {
        getToken(1).correct("correction", -0.8, false);
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getIndex(), is(1));
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getConfidence(), is(-0.8));
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getDataType(), is("OCR-D-CIS-POST-CORRECTION"));
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getDataTypeDetails(), is("correction: Correction")); // case handling
        assertThat(getToken(1).getTextRegion().getTextEquivs().get(0).getUnicode(), is("Seneribus"));
    }

    private BaseOCRToken getToken(int n) throws Exception {
        return (BaseOCRToken) workspace.getBaseOCRTokenReader("OCR-D-EVAL").read().get(n);
    }
}
