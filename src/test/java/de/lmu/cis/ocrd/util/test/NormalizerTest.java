package de.lmu.cis.ocrd.util.test;

import de.lmu.cis.ocrd.util.Normalizer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NormalizerTest {
	@Test
	public void testLeadingWhitespace() {
		final String want = "string with leading white space";
		final String got = Normalizer.normalize("  " + want);
		assertThat(got, is(want));
	}

	@Test
	public void testSubsequentWhitespace() {
		final String want = "string with subsequent white space";
		final String got = Normalizer.normalize(want + "  ");
		assertThat(got, is(want));
	}

	@Test
	public void testNormalizeAllWhitespace() {
		final String want = "string with normalized white space";
		final String got = Normalizer.normalize(
				"  string  with  normalized     \t\t\n white space \n \t \n"
		);
		assertThat(got, is(want));
	}

	@Test
	public void testNormalizePunctuation() {
		final String want = "string with punctuation and white-space";
		final String got = Normalizer.normalize(
				"  string,  with  punctuation and white-space."
		);
		assertThat(got, is(want));
	}

	@Test
	public void testNormalizeWithRoundR() {
		final String want = "Grammatice Salutem et ingenuos laboꝛes";
		final String got = Normalizer.normalize(
				"Grammatice. Salutem. et ingenuos laboꝛes."
		);
		assertThat(got, is(want));
	}

	@Test
	public void testWithCombiningCharacter() {
		final String want = "foo̰bar";
		final String got = Normalizer.normalize(want);
		assertThat(got, is(want));
	}

	@Test
	public void testWithHistoricalCharacter1() {
		final String want = "Mfs zeſ⸗o fc⸗⸗oe";
		final String got = Normalizer.normalize(want);
		assertThat(got, is(want));
	}

	@Test
	public void testWithSpecialChars() {
		final String want = "String with special characters";
		final String got = Normalizer.normalize(
				"#String with special characters $ # $");
		assertThat(got, is(want));
	}

	@Test
	public void testEqualsIgnoreCase() {
		final String a = "ſeer";
		final String b = "seer";
		// equalIgnoreCase compares to true for all a,b in str1,str2
		// if a == b or toLower(a) == toLower(b) or toUpper(a) == toUpper(b).
		// For ſ: toUpper(ſ) == S.
		assertThat(a.equalsIgnoreCase(b), is(true));
	}

	@Test
	public void testToLowerCaseEquals() {
		final String a = "ſeer";
		final String b = "seer";
		// We need to use this comparison to compare two strings
		// ignoring case (see above).
		assertThat(a.toLowerCase().equals(b), is(false));
	}
}
