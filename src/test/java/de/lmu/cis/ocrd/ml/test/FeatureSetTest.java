package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.ml.Feature;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Value;
import de.lmu.cis.pocoweb.Token;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeatureSetTest {
    private class MockValue implements Value {
    	private final double n;
    	MockValue(double n) {
    		this.n = n;
		}
		public boolean isBoolean() {
    		return false;
		}
		public boolean getBoolean() {
    		return false;
		}
		public double getDouble() {
    		return n;
		}

	}
	private class MockFeature implements Feature {
		private final Value val;

		public MockFeature(double n) {
			this.val = new MockValue(n);
		}

		@Override
		public Value calculate(Token token) {
			return val;
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
		assertThat(features.calculateFeatureVector(new Token()).get(0).getDouble(), is(1.0));
	}

	@Test
	public void testLenFeatureVector() {
		assertThat(features.calculateFeatureVector(new Token()).size(), is(2));
	}

	@Test
	public void testSecondFeatureValue() {
		assertThat(features.calculateFeatureVector(new Token()).get(1).getDouble(), is(3.0));
	}
}
