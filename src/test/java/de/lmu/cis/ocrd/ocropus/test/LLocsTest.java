package de.lmu.cis.ocrd.ocropus.test;

import de.lmu.cis.ocrd.ocropus.LLocs;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

    @Test
    public void testReadLLocsPath1() throws Exception {
        final LLocs llocs = LLocs.read(Paths.get("src/test/resources/ocropus/0001/0001.llocs.1"));
        assertThat(llocs.toString(), is("ab cd ef"));
    }

    @Test
    public void testReadLLocsPath2() throws Exception {
        final LLocs llocs = LLocs.read(Paths.get("src/test/resources/ocropus/0001/0001.llocs.2"));
        assertThat(llocs.toString(), is("abc d ef"));
    }

    @Test
    public void testReadLLocsPath3() throws Exception {
        final LLocs llocs = LLocs.read(Paths.get("src/test/resources/ocropus/0001/0001.llocs.3"));
        assertThat(llocs.toString(), is("abc def"));
    }

    private static InputStream isFromString(String string) {
        return new ByteArrayInputStream(string.getBytes());
    }
}
