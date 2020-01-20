package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.CandidateUnigramFeature;
import de.lmu.cis.ocrd.ml.features.Feature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CandidateUnigramFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() throws Exception {
		super.init();
		f = new CandidateUnigramFeature(lm,"candidate-unigram-feature");
	}
	@Test
	public void testMissingTokenFrequencyEqualsZero() {
		assertThat(f.calculate(getCandidateToken(1, 1), 0, 2), is(0.0));

	}

	@Test
	public void testFeatureHandlesAnyOCR() {
		assertThat(f.handlesOCR(0, 2), is(true));
		assertThat(f.handlesOCR(1, 2), is(true));

	}

	@Test
	public void testTokenFrequencyIsOK() {
		assertThat(f.calculate(getCandidateToken(0, 0), 1, 2), is(0.1));
	}
}
