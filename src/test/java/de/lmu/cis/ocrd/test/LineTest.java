package de.lmu.cis.ocrd.test;

import de.lmu.cis.ocrd.SimpleLine;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class LineTest {
    @Test
    public void testAll() {
        SimpleLine line = SimpleLine.normalized("--Please-- normalize (this)", 0.27);
        assertThat(line.getNormalized(), is("Please normalize this"));
        int i = 0;
        for (int ignored : line.getNormalized().codePoints().toArray()) {
            assertThat(line.getConfidenceAt(i), is(0.27));
           i++;
        }
    }
}
