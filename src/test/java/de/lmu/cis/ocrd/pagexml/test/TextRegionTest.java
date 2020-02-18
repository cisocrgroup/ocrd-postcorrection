package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.*;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TextRegionTest {
    private final static Path pagexml = Paths.get("src/test/resources/page.xml");
    private Page page;

    @Before
    public void init() throws Exception {
        page = Page.open(pagexml);
    }

    @Test
    public void testGetTextEquiv() throws Exception {
        final String want = "Berliniſche Monatsſchrift.";
        final String got = getTextRegion(0).getTextEquivs().get(0).getUnicode();
        assertThat(got, is(want));
    }

    @Test
    public void testAppendTextEquiv() {
        int i = 0;
        final double wantConfidence = 0.42;
        final String wantDataType = "test-data-type";
        final String wantDataTypeDetails = "test-data-type-details";
        for (Line line: page.getLines()) {
            i++;
            line.appendNewTextEquiv()
                    .withIndex(i)
                    .withConfidence(wantConfidence)
                    .withDataType(wantDataType)
                    .withDataTypeDetails(wantDataTypeDetails);
            for (Word word: line.getWords()) {
                i++;
                word.appendNewTextEquiv()
                        .withIndex(i)
                        .withConfidence(wantConfidence)
                        .withDataType(wantDataType)
                        .withDataTypeDetails(wantDataTypeDetails);
            }
        }

        i = 0;
        for (Line line: page.getLines()) {
            i++;
            TextEquiv lte = line.getTextEquivs().get(line.getTextEquivs().size()-1);
            assertThat(lte.getIndex(), is(i));
            assertThat(lte.getConfidence(), is(wantConfidence));
            assertThat(lte.getDataType(), is(wantDataType));
            assertThat(lte.getDataTypeDetails(), is(wantDataTypeDetails));
            for (Word word: line.getWords()) {
                i++;
                lte = word.getTextEquivs().get(word.getTextEquivs().size() - 1);
                assertThat(lte.getIndex(), is(i));
                assertThat(lte.getConfidence(), is(wantConfidence));
                assertThat(lte.getDataType(), is(wantDataType));
                assertThat(lte.getDataTypeDetails(), is(wantDataTypeDetails));
            }
        }
    }

    private TextRegion getTextRegion(int i) throws Exception {
        int j = 0;
        for (Line line: page.getLines()) {
            if (j == i) {
                return line;
            }
            j++;
            for (Word word: line.getWords()) {
                if (j == i) {
                    return word;
                }
                j++;
            }
        }
        throw new Exception("cannot find text region: " + i);
    }
}
