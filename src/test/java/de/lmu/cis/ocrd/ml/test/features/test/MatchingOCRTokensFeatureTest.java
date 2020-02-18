package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.MatchingOCRTokensFeature;
import de.lmu.cis.ocrd.ml.OCRToken;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MatchingOCRTokensFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() throws Exception {
		f = new MatchingOCRTokensFeature("matching-ocr-tokens");
	}
	@Test
	public void testFeatureHandlesOnlyMasterOCR() {
		assertThat(f.handlesOCR(0, 2), is(false));
		assertThat(f.handlesOCR(1, 2), is(true));
	}

	@Test
	public void testFeatureCountsMatchingOCRTokens() {
		final OCRToken t = getToken(1);
		assertThat(f.calculate(t, 1, 2), is(1.0));
	}

	@Test
	public void testFeatureCountsMatchingOCRTokensNoMatch() {
		final OCRToken t = getToken(2);
		assertThat(f.calculate(t, 1, 2), is(0.0));
	}
}
