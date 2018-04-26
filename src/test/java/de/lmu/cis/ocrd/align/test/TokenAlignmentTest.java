package de.lmu.cis.ocrd.align.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.align.TokenAlignment;

public class TokenAlignmentTest {
	@Test
	public void test2alignments() {
		final String a = "abc def";
		final String b = "a b c def";
		TokenAlignment tokens = new TokenAlignment(a).add(b);
		assertThat(tokens.size(), is(2));
		assertThat(tokens.get(0).toString(), is("abc|a,b,c"));
		assertThat(tokens.get(1).toString(), is("def|def"));
	}

	@Test
	public void test3alignments() {
		final String a = "abcd ef gh";
		final String b = "ab cd efgh";
		final String c = "ab cd ef gh";
		TokenAlignment tokens = new TokenAlignment(a).add(b).add(c);
		assertThat(tokens.size(), is(3));
		assertThat(tokens.get(0).toString(), is("abcd|ab,cd|ab,cd"));
		assertThat(tokens.get(1).toString(), is("ef|efgh|ef"));
		assertThat(tokens.get(2).toString(), is("gh|efgh|gh"));
	}

	@Test
	public void testAlignToSelf() {
		final String a = "abc def ghi";
		TokenAlignment tokens = new TokenAlignment(a).add(a);
		assertThat(tokens.size(), is(3));
		assertThat(tokens.get(0).toString(), is("abc|abc"));
		assertThat(tokens.get(1).toString(), is("def|def"));
		assertThat(tokens.get(2).toString(), is("ghi|ghi"));
	}
}
