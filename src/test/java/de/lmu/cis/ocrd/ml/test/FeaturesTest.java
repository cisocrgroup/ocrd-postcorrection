package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class FeaturesTest {
	private Token t1, t2;

	@Before
	public void init() {
		final SimpleLine masterOCRLine = SimpleLine.normalized("tvo t0ken", 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8);
		t1 = new Token(masterOCRLine.getWord(0, "tvo").get(), 1).withGT("two");
		t2 = new Token(masterOCRLine.getWord(0, "t0ken").get(), 2).withGT("token");
	}

	@Test
	public void testMinOCRConfidenceFeature() {
		final Feature feature = new MinOCRConfidenceFeature("name");
		assertThat(feature.calculate(t1, 0, 1), is(0.0));
		assertThat(feature.calculate(t2, 0, 1), is(0.4));
	}

	@Test
	public void testMaxOCRConfidenceFeature() {
		final Feature feature = new MaxOCRConfidenceFeature("name");
		assertThat(feature.calculate(t1, 0, 1), is(0.2));
		assertThat(feature.calculate(t2, 0, 1), is(0.8));
	}

	@Test
	public void testTokenCaseFeature() {
		final Feature feature = new TokenCaseClassFeature("TokenCase");
		assertThat(feature.calculate(t1, 0, 1), is("all-lower-case"));
		assertThat(feature.calculate(t2, 0, 1), is("mixed-case"));
	}

	@Test
	public void testTokenLengthFeature() {
		final Feature feature = new TokenLengthClassFeature("TokenCase", 3, 8, 13);
		assertThat(feature.calculate(t1, 0, 1), is("short-token"));
		assertThat(feature.calculate(t2, 0, 1), is("medium-token"));
	}
}
