package de.lmu.cis.ocrd.test;

import de.lmu.cis.ocrd.SimpleLine;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LineTest {
    @Test
    public void testSimple() {
        SimpleLine line = SimpleLine.normalized("--Please-- normalize (this)", 0.27);
        assertThat(line.getNormalized(), is("Please normalize this"));
        int i = 0;
        for (int ignored : line.getNormalized().codePoints().toArray()) {
            assertThat(line.getConfidenceAt(i), is(0.27));
            i++;
        }
    }

    @Test
    public void testUnicode() {
        SimpleLine line = SimpleLine.normalized("Ill Nin\u0303o.", 0.27);
        assertThat(line.getNormalized(), is("Ill Nin\u0303o"));
        int i = 0;
        for (int ignored : line.getNormalized().codePoints().toArray()) {
            assertThat(line.getConfidenceAt(i), is(0.27));
            i++;
        }
    }

    @Test
    public void testMulti() {
        Double[] cs = new Double[]{0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        SimpleLine line = SimpleLine.normalized("Ill Nin\u0303o.", new ArrayList<>(Arrays.asList(cs)));
        int i = 0;
        for (int ignored : line.getNormalized().codePoints().toArray()) {
            assertThat(line.getConfidenceAt(i), is(cs[i]));
            i++;
        }
    }
}
