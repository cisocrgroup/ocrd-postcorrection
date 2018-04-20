package de.lmu.cis.ocrd.ml.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.lmu.cis.ocrd.ml.FreqMap;

public class FreqMapTest {
	private FreqMap<String> freqs;

	@Before
	public void init() {
		freqs = new FreqMap<String>();
		freqs.add("A");
		freqs.add("A");
		freqs.add("A");
		freqs.add("B");
		freqs.add("B");
		freqs.add("C");
	}

	@Test
	public void test0() throws Exception {
		freqs = new FreqMap<String>();
		assertThat(freqs.getAbsolute("A"), is(0));
		assertThat(freqs.getRelative("A"), is(0.0));
	}

	@Test
	public void testA() throws Exception {
		assertThat(freqs.getAbsolute("A"), is(3));
		assertThat(freqs.getRelative("A"), is(3.0 / 6.0));
	}

	@Test
	public void testB() throws Exception {
		assertThat(freqs.getAbsolute("B"), is(2));
		assertThat(freqs.getRelative("B"), is(2.0 / 6.0));
	}

	@Test
	public void testC() throws Exception {
		assertThat(freqs.getAbsolute("C"), is(1));
		assertThat(freqs.getRelative("C"), is(1.0 / 6.0));
	}

	@Test
	public void testD() throws Exception {
		assertThat(freqs.getAbsolute("D"), is(0));
		assertThat(freqs.getRelative("D"), is(0.0));
	}

	@Test
	public void testTotal() throws Exception {
		assertThat(freqs.getTotal(), is(6));
	}
}
