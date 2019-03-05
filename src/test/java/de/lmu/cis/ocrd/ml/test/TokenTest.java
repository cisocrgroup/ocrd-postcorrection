package de.lmu.cis.ocrd.ml.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.ml.Token;

public class TokenTest {

	@Test
	public void testWithNoGT() {
		assertThat(Token.create("a", 1).getGT().isPresent(), is(false));
	}

	@Test
	public void testWithGT() {
		assertThat(Token.create("a", 1).withGT("b").getGT().isPresent(), is(true));
		assertThat(Token.create("a", 1).withGT("b").getGT().get(), is("b"));
	}

	@Test
	public void testWithFirstOtherOCR() {
		assertThat(Token.create("a", 1).addOCR(Word.create("b")).getNumberOfOtherOCRs(), is(1));
		assertThat(Token.create("a", 1).addOCR(Word.create("b")).getSlaveOCR(0).toString(), is("b"));
	}

	@Test
	public void testWithSecondOtherOCR() {
		assertThat(Token.create("a", 1).addOCR(Word.create("b")).addOCR(Word.create("c")).getNumberOfOtherOCRs(),
				is(2));
		assertThat(Token.create("a", 1).addOCR(Word.create("b")).addOCR(Word.create("c")).getSlaveOCR(0).toString(),
				is("b"));
		assertThat(Token.create("a", 1).addOCR(Word.create("b")).addOCR(Word.create("c")).getSlaveOCR(1).toString(),
				is("c"));
	}
}
