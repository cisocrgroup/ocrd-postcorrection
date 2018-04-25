package de.lmu.cis.ocrd.align.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.align.TokenAlignment;

public class TokenAlignmentTest {
	@Test
	public void test() {
		final String a = "abc def";
		final String b = "a b c def";
		TokenAlignment tokens = new TokenAlignment(a).add(b);
		assertThat(tokens.size(), is(2));
		assertThat(tokens.get(0).toString(), is("abc|a,b,c"));
		assertThat(tokens.get(1).toString(), is("def|def"));
	}
}
