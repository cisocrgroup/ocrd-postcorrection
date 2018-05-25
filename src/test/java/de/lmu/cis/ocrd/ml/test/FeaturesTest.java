package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.MaxOCRConfidenceFeature;
import de.lmu.cis.ocrd.ml.features.MinOCRConfidenceFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
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
		final Feature feature = new MinOCRConfidenceFeature("name", 0);
		assertThat(feature.calculate(t1, 0, 1), is(0.0));
		assertThat(feature.calculate(t2, 0, 1), is(0.4));
	}

	@Test
	public void testMaxOCRConfidenceFeature() {
		final Feature feature = new MaxOCRConfidenceFeature("name", 0);
		assertThat(feature.calculate(t1, 0, 1), is(0.2));
		assertThat(feature.calculate(t2, 0, 1), is(0.8));
	}


}
