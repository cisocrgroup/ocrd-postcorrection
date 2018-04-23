package de.lmu.cis.ocrd.ml.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.lmu.cis.ocrd.ml.Feature;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.pocoweb.Token;

public class FeatureSetTest {
	private class MockFeature implements Feature {
		private final double n;

		public MockFeature(double n) {
			this.n = n;
		}

		@Override
		public double calculate(Token token) {
			return this.n;
		}
	}

	private FeatureSet features;

	@Before
	public void init() {
		features = new FeatureSet().add(new MockFeature(1)).add(new MockFeature(3));
	}

	@Test
	public void testFeatureSetSize() {
		assertThat(features.size(), is(2));
	}

	@Test
	public void testFirstFeatureValue() {
		assertThat(features.calculateFeatureVector(new Token()).get(0), is(1.0));
	}

	@Test
	public void testLenFeatureVector() {
		assertThat(features.calculateFeatureVector(new Token()).size(), is(2));
	}

	@Test
	public void testSecondFeatureValue() {
		assertThat(features.calculateFeatureVector(new Token()).get(1), is(3.0));
	}
}
