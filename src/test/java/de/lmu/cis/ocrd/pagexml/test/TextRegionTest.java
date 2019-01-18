package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.TextRegion;
import de.lmu.cis.ocrd.pagexml.Word;
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
