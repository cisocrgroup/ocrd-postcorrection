package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.ml.FreqMap;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FreqMapTest {
	private FreqMap freqs;

	@Before
	public void init() {
		freqs = new FreqMap();
		freqs.add("A");
		freqs.add("A");
		freqs.add("A");
		freqs.add("B");
		freqs.add("B");
		freqs.add("C");
	}

    @Test
    public void testLowerCases() {
        assertThat(freqs.getAbsolute("a"), is(3));
        assertThat(freqs.getAbsolute("b"), is(2));
        assertThat(freqs.getAbsolute("c"), is(1));
        assertThat(freqs.getAbsolute("d"), is(0));
    }

	@Test
	public void test0() {
		freqs = new FreqMap();
		assertThat(freqs.getAbsolute("A"), is(0));
		assertThat(freqs.getRelative("A"), is(0.0));
	}

	@Test
	public void testA() {
		assertThat(freqs.getAbsolute("A"), is(3));
		assertThat(freqs.getRelative("A"), is(3.0 / 6.0));
	}

	@Test
	public void testB() {
		assertThat(freqs.getAbsolute("B"), is(2));
		assertThat(freqs.getRelative("B"), is(2.0 / 6.0));
	}

	@Test
	public void testC() {
		assertThat(freqs.getAbsolute("C"), is(1));
		assertThat(freqs.getRelative("C"), is(1.0 / 6.0));
	}

	@Test
	public void testD() {
		assertThat(freqs.getAbsolute("D"), is(0));
		assertThat(freqs.getRelative("D"), is(0.0));
	}

	@Test
	public void testTotal() {
		assertThat(freqs.getTotal(), is(6));
	}
}
