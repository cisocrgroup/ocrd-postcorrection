package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.ml.Token;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TokenTest {

    @Test
    public void testWithNoGT() {
        assertThat(Token.create("a").getGT().isPresent(), is(false));
    }

    @Test
    public void testWithGT() {
        assertThat(Token.create("a").withGT("b").getGT().isPresent(), is(true));
        assertThat(Token.create("a").withGT("b").getGT().get(), is("b"));
    }

    @Test
    public void testWithFirstOtherOCR() {
        assertThat(Token.create("a").addOCR(Word.create("b")).getNumberOfOtherOCRs(), is(1));
        assertThat(Token.create("a").addOCR(Word.create("b")).getOtherOCRAt(0).toString(), is("b"));
    }

    @Test
    public void testWithSecondOtherOCR() {
        assertThat(Token.create("a").addOCR(Word.create("b")).addOCR(Word.create("c")).getNumberOfOtherOCRs(), is(2));
        assertThat(Token.create("a").addOCR(Word.create("b")).addOCR(Word.create("c")).getOtherOCRAt(0).toString(), is("b"));
        assertThat(Token.create("a").addOCR(Word.create("b")).addOCR(Word.create("c")).getOtherOCRAt(1).toString(), is("c"));
    }
}
