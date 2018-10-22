package de.lmu.cis.ocrd.ml.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.NamedDoubleFeature;
import de.lmu.cis.ocrd.ml.features.OCRToken;

public class FeatureSetTest {
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
		assertThat(features.calculateFeatureVector(Token.create("a", 1), 1).get(0), is(1.0));
	}

	@Test
	public void testLenFeatureVector() {
		assertThat(features.calculateFeatureVector(Token.create("a", 1), 1).size(), is(2));
	}

	@Test
	public void testSecondFeatureValue() {
		assertThat(features.calculateFeatureVector(Token.create("a", 1), 1).get(1), is(3.0));
	}

	private class MockFeature extends NamedDoubleFeature {
		private final double val;

		MockFeature(double n) {
			super("MockFeature");
			this.val = n;
		}

		@Override
		public boolean handlesOCR(int i, int n) {
			return handlesOnlyMasterOCR(i, n);
		}

		@Override
		protected double doCalculate(OCRToken token, int ignored1, int ignored2) {
			return val;
		}
	}
}
