package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.ml.PatternSet;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PatternSetTest {
    private PatternSet fromString(String patterns) throws Exception {
        return PatternSet.read(new ByteArrayInputStream(patterns.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testIgnoreComments() throws Exception {
        final PatternSet ps = fromString("# comment\n# comment\na:b#comment\nc:d # comment  \n");
        assertThat(ps.contains("a:b"), is(true));
        assertThat(ps.contains("c:d"), is(true));
        assertThat(ps.size(), is(2));
    }

    @Test
    public void testRightSideEmpty() throws Exception {
        final PatternSet ps = fromString(" x: # comment");
        assertThat(ps.contains("x:"), is(true));
        assertThat(ps.size(), is(1));
    }

    @Test
    public void testLeftSideEmpty() throws Exception {
        final PatternSet ps = fromString(" :x  # comment");
        assertThat(ps.contains(":x"), is(true));
        assertThat(ps.size(), is(1));
    }

    @Test(expected = Exception.class)
    public void testThrowsOnInvalidPattern() throws Exception {
        final PatternSet ps = fromString("ixy # comment");
    }
}
