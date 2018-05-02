package de.lmu.cis.ocrd.ml.test;

import org.junit.Test;
import de.lmu.cis.ocrd.ml.Token;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class TokenTest {

    @Test
    public void testWithNoGT() {
        assertThat(new Token("a").getGT().isPresent(), is(false));
    }

    @Test
    public void testWithGT() {
        assertThat(new Token("a").withGT("b").getGT().isPresent(), is(true));
        assertThat(new Token("a").withGT("b").getGT().get(), is("b"));
    }

    @Test
    public void testWithFirstOtherOCR() {
        assertThat(new Token("a").addOCR("b").getNumberOfOtherOCRs(), is(1));
        assertThat(new Token("a").addOCR("b").getOtherOCRAt(0), is("b"));
    }

    @Test
    public void testWithSecondOtherOCR() {
        assertThat(new Token("a").addOCR("b").addOCR("c").getNumberOfOtherOCRs(), is(2));
        assertThat(new Token("a").addOCR("b").addOCR("c").getOtherOCRAt(0), is("b"));
        assertThat(new Token("a").addOCR("b").addOCR("c").getOtherOCRAt(1), is("c"));
    }
}
