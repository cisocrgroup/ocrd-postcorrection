package de.lmu.cis.ocrd.ocropus.test;

import de.lmu.cis.ocrd.ocropus.LLocs;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class LLocsTest {
    @Test
    public void testReadLLocsString() throws Exception {
        final LLocs llocs = LLocs.read(isFromString("a\t0.1\t0.2\nb\t0.3\t0.4\n"));
        assertThat(llocs.toString(), is("ab"));
        assertThat(llocs.length(), is(2));
        assertThat(llocs.at(0).getC(), is((int)'a'));
        assertThat(llocs.at(1).getC(), is((int)'b'));
    }

    @Test
    public void testReadLLocsConfidence() throws Exception {
        final LLocs llocs = LLocs.read(isFromString("a\t0.1\t0.2\nb\t0.3\t0.4\n"));
        assertThat(llocs.length(), is(2));
        assertThat(llocs.at(0).getConfidence(), is(0.2));
        assertThat(llocs.at(1).getConfidence(), is(0.4));
    }

    private static InputStream isFromString(String string) {
        return new ByteArrayInputStream(string.getBytes());
    }
}
