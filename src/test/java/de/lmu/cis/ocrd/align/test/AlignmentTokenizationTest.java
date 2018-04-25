package de.lmu.cis.ocrd.align.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.align.Tokenizer;

public class AlignmentTokenizationTest {
	private static class Pair {
		@SuppressWarnings("unused")
		String first, second;
	}

	private static ArrayList<Pair> align(String a, String b) {
		final Graph g = new Graph(a, b);
		final Tokenizer t = g.getTokenizer();
		final ArrayList<Pair> pairs = new ArrayList<Pair>();
		t.eachPair((s1, s2) -> {
			pairs.add(new Pair());
			pairs.get(pairs.size() - 1).first = s1;
			pairs.get(pairs.size() - 1).second = s2;
			// System.out.println("a = '" + s1 + "' b = " + s2 + "'");
		});
		return pairs;
	}

	@Test
	public void testDeletion() {
		final String a = "here is a deletion my friend";
		final String b = "here is a del my friend";
		assertThat(align(a, b).size(), is(6));
		assertThat(align(a, b).get(3).first, is("deletion"));
		assertThat(align(a, b).get(3).second, is("del"));
	}

	@Test
	public void testFirstABPair() {
		final String a = "schwärmt Der Name Belgien aber so uralt das Wort auch ist";
		final String b = "ſchw rmt Der Name Belgien aber ſo uralt das Wort auch iſt";
		assertThat(align(a, b).get(0).first, is("schwärmt"));
		assertThat(align(a, b).get(0).second, is("ſchw"));
	}

	@Test
	public void testLastABPair() {
		final String a = "schwärmt Der Name Belgien aber so uralt das Wort auch ist";
		final String b = "ſchw rmt Der Name Belgien aber ſo uralt das Wort auch iſt";
		assertThat(align(a, b).get(11).first, is("ist"));
		assertThat(align(a, b).get(11).second, is("iſt"));
	}

	@Test
	public void testLenAB() {
		final String a = "schwärmt Der Name Belgien aber so uralt das Wort auch ist";
		final String b = "ſchw rmt Der Name Belgien aber ſo uralt das Wort auch iſt";
		assertThat(align(a, b).size(), is(12));
	}

	@Test
	public void testLenBA() {
		final String a = "schwärmt Der Name Belgien aber so uralt das Wort auch ist";
		final String b = "ſchw rmt Der Name Belgien aber ſo uralt das Wort auch iſt";
		assertThat(align(b, a).size(), is(12));
	}

}
