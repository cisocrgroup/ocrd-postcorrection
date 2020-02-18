package de.lmu.cis.ocrd.util.test;

import de.lmu.cis.ocrd.util.StringCorrector;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StringCorrectorTest {
	@Test
	public void testSimpleCapitalization() {
		assertThat(new StringCorrector("Foo").correctWith("bar"), is("Bar"));
	}

	@Test
	public void testAllUpperCase() {
		assertThat(new StringCorrector("FOO").correctWith("bar"), is("BAR"));
	}

	@Test
	public void testShortSimpleCapitalization() {
		assertThat(new StringCorrector("Foo").correctWith("ba"), is("Ba"));
	}

	@Test
	public void testShortAllUpperCase() {
		assertThat(new StringCorrector("FOO").correctWith("ba"), is("BA"));
	}

	@Test
	public void testLongSimpleCapitalization() {
		assertThat(new StringCorrector("Foo").correctWith("baaar"), is("Baaar"));
	}

	@Test
	public void testLongAllUpperCase() {
		assertThat(new StringCorrector("FOO").correctWith("baaar"), is("BAAAR"));
	}

	@Test
	public void testWithPrefixAndSuffix() {
		assertThat(new StringCorrector("/Foo,").correctWith("bar"), is("/Bar,"));
	}

}
