package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Word;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SchulteTest {
    private final static Path dir = Paths.get("src/test/resources/OCR-D-PROFILE-schultes_formular_1633");
    private List<Page> pages;

    @Before
    public void init() throws Exception {
        pages = new ArrayList<>();
        File[] files = dir.toFile().listFiles();
        for (File file: files) {
            pages.add(Page.parse(new FileInputStream(file)));
        }
    }

    @Test
    public void testNumberOfPages() {
        assertThat(pages.size(), is(3));
    }

    @Test
    public void readAllTokens() {
        int i = 0;
        for (Page page: pages) {
            for (Line line: page.getLines()) {
                for (Word word: line.getWords()) {
                    List<String> u = word.getUnicodeNormalized();
                    for (String x : u) {
                        i++;
                    }
                }
            }
        }
        assertThat(i, is(9462));
    }

}
