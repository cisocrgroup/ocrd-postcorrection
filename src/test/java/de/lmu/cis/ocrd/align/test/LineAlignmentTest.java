package de.lmu.cis.ocrd.align.test;

import de.lmu.cis.ocrd.align.Lines;
import org.junit.Test;

import java.util.List;
import java.util.StringJoiner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LineAlignmentTest {

    @Test
    public void testWordAlignments() {
        String[] lines = {
                "first second third word",
                "frst sec ond thirdword",
                "fir st second thrid word",
                "first second thrd word",
        };
        Lines.Alignment alignment = Lines.align(lines);
        assertThat(alignment.wordAlignments.size(), is(4));

        // first word alignment
        assertThat(alignment.wordAlignments.get(0).master, is("first"));
        assertThat(alignment.wordAlignments.get(0).alignments.size(), is(3));
        assertThat(join(alignment.wordAlignments.get(0).alignments.get(0)), is("frst"));
        assertThat(join(alignment.wordAlignments.get(0).alignments.get(1)), is("fir st"));
        assertThat(join(alignment.wordAlignments.get(0).alignments.get(2)), is("first"));

        // second word alignment
        assertThat(alignment.wordAlignments.get(1).master, is("second"));
        assertThat(alignment.wordAlignments.get(1).alignments.size(), is(3));
        assertThat(join(alignment.wordAlignments.get(1).alignments.get(0)), is("sec ond"));
        assertThat(join(alignment.wordAlignments.get(1).alignments.get(1)), is("second"));
        assertThat(join(alignment.wordAlignments.get(1).alignments.get(2)), is("second"));

        // third word alignment
        assertThat(alignment.wordAlignments.get(2).master, is("third"));
        assertThat(alignment.wordAlignments.get(2).alignments.size(), is(3));
        assertThat(join(alignment.wordAlignments.get(2).alignments.get(0)), is("thirdword"));
        assertThat(join(alignment.wordAlignments.get(2).alignments.get(1)), is("thrid"));
        assertThat(join(alignment.wordAlignments.get(2).alignments.get(2)), is("thrd"));

        // fourth word alignment
        assertThat(alignment.wordAlignments.get(3).master, is("word"));
        assertThat(alignment.wordAlignments.get(3).alignments.size(), is(3));
        assertThat(join(alignment.wordAlignments.get(3).alignments.get(0)), is("thirdword"));
        assertThat(join(alignment.wordAlignments.get(3).alignments.get(1)), is("word"));
        assertThat(join(alignment.wordAlignments.get(3).alignments.get(2)), is("word"));
    }

    private static String join(List<String> strs) {
        StringJoiner sj = new StringJoiner(" ");
        strs.forEach(sj::add);
        return sj.toString();
    }
}
