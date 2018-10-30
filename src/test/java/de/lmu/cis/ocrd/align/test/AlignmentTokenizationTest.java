package de.lmu.cis.ocrd.align.test;

import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.align.Tokenizer;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class AlignmentTokenizationTest {
	private static ArrayList<Pair> align(String a, String b) {
		final Graph g = new Graph(a, b);
		final Tokenizer t = g.getTokenizer();
		final ArrayList<Pair> pairs = new ArrayList<Pair>();
		t.eachPair((s1, s2, x, y) -> {
			pairs.add(new Pair());
			pairs.get(pairs.size() - 1).first = s1;
			pairs.get(pairs.size() - 1).second = s2;
		});
		return pairs;
	}

	@Test
	public void testAlignToSelf() {
		final String a = "one two three tokens";
		ArrayList<Pair> self = align(a, a);
		assertThat(self.size(), is(4));
		for (Pair p : self) {
			assertThat(p.first, is(p.second));
		}
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

	private static class Pair {
		String first, second;
	}

}
