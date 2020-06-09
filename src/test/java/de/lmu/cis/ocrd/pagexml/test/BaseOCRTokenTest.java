package de.lmu.cis.ocrd.pagexml.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.pagexml.BaseOCRToken;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Workspace;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BaseOCRTokenTest {
    private static final String INPUT_FILE_GROUP = "OCR-D-EVAL";
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
        getToken(2).correct("correction", -0.8, false);
        assertThat(getToken(2).getTextRegion().getTextEquivs().get(0).getIndex(), is(1));
        assertThat(getToken(2).getTextRegion().getTextEquivs().get(0).getConfidence(), is(-0.8));
        assertThat(getToken(2).getTextRegion().getTextEquivs().get(0).getDataType(), is("OCR-D-CIS-POST-CORRECTION"));
        assertThat(getToken(2).getTextRegion().getTextEquivs().get(0).getDataTypeDetails(), is("correction: correction.")); // case and punctuation handling
        assertThat(getToken(2).getTextRegion().getTextEquivs().get(0).getUnicode(), is("nominum."));
    }

    @Test
    public void testLineCorrection() throws Exception {
        List<Page> pages = workspace.getPages(INPUT_FILE_GROUP);
        getToken(1).correct("correction", 0.8, true);
        getToken(2).correct("correction", -0.8, false);
        pages.get(0).correctLinesAndRegions();
        assertThat(pages.get(0).getLines().get(0).getUnicode().get(0), is("e Correction nominum."));
    }

    @Test
    public void testRegionCorrection() throws Exception {
        List<Page> pages = workspace.getPages(INPUT_FILE_GROUP);
        getToken(1).correct("correction", -0.8, false);
        getToken(2).correct("correction", 0.8, true);
        pages.get(0).correctLinesAndRegions();
        assertThat(pages.get(0).getTextRegions().get(0).getUnicode().get(0), is("e Seneribus correction."));
    }

    private BaseOCRToken getToken(int n) throws Exception {
        return (BaseOCRToken) workspace.getBaseOCRTokenReader(INPUT_FILE_GROUP).read().get(n);
    }
}
