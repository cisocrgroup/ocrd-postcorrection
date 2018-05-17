package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.ml.CharacterNGrams;
import de.lmu.cis.ocrd.ml.FreqMap;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CharacterNGramsTest {
    private FreqMap nGrams;

    @Before
    public void init() throws Exception {
        nGrams = CharacterNGrams.fromCSV("src/test/resources/nGrams.csv");
    }

    @Test
    public void testTotal() {
        assertThat(nGrams.getTotal(), is(36));
    }

    @Test
    public void testABC() {
        assertThat(nGrams.getAbsolute("abc"), is(20));
    }
    @Test
    public void testDEF() {
        assertThat(nGrams.getAbsolute("def"), is(3));
    }
    @Test
    public void testNDS() {
        assertThat(nGrams.getAbsolute("nd$"), is(4));
    }
}
