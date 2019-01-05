package de.lmu.cis.ocrd.test;

import de.lmu.cis.ocrd.SimpleLine;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
public class LineTest {
	@SuppressWarnings("unused")
	@Test
	public void testSimple() {
		SimpleLine line = SimpleLine.normalized("--Please-- normalize (this)", 0.27);
		assertThat(line.getNormalized(), is("Please normalize this"));
		int i = 0;
		for (int unused : line.getNormalized().codePoints().toArray()) {
			assertThat(line.getConfidenceAt(i), is(0.27));
			i++;
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testUnicode() {
		SimpleLine line = SimpleLine.normalized("Ill Nin\u0303o.", 0.27);
		assertThat(line.getNormalized(), is("Ill Nin\u0303o"));
		int i = 0;
		for (int unused : line.getNormalized().codePoints().toArray()) {
			assertThat(line.getConfidenceAt(i), is(0.27));
			i++;
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testMulti() {
		Double[] cs = new Double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
		SimpleLine line = SimpleLine.normalized("Ill Nin\u0303o.", new ArrayList<>(Arrays.asList(cs)));
		int i = 0;
		for (int unused : line.getNormalized().codePoints().toArray()) {
			assertThat(line.getConfidenceAt(i), is(cs[i]));
			i++;
		}
	}

	@Test
	public void testGetWordFind() {
		SimpleLine line = SimpleLine.normalized("first second third", 1);
		assertThat(line.getWord("first").isPresent(), is(true));
		assertThat(line.getWord("first").get().toString(), is("first"));
		assertThat(line.getWord("second").isPresent(), is(true));
		assertThat(line.getWord("second").get().toString(), is("second"));
		assertThat(line.getWord("third").isPresent(), is(true));
		assertThat(line.getWord("third").get().toString(), is("third"));
		assertThat(line.getWord("fourth").isPresent(), is(false));
	}

	@Test
	public void testGetWordConfidences() {
		SimpleLine line = SimpleLine.normalized("first second third", 0.8);
		assertThat(line.getWord("first").isPresent(), is(true));
		assertThat(line.getWord("first").get().getCharacterConfidenceAt(0), is(0.8));
		assertThat(line.getWord("first").get().getCharacterConfidenceAt(1), is(0.8));
	}

	@Test
	public void testGetWordWithOffset() {
		Double[] cs = new Double[]{0.1, 0.1, 0.0, 0.2, 0.2};
		SimpleLine line = SimpleLine.normalized("aa aa", new ArrayList<>(Arrays.asList(cs)));
		assertThat(line.getWord(0, "aa").isPresent(), is(true));
		assertThat(line.getWord(0, "aa").get().getCharacterConfidenceAt(0), is(0.1));
		assertThat(line.getWord(2, "aa").isPresent(), is(true));
		assertThat(line.getWord(2, "aa").get().getCharacterConfidenceAt(0), is(0.2));
	}
}
