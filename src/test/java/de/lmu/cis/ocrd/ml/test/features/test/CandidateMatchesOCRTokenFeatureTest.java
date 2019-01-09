package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.CandidateMatchesOCRTokenFeature;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CandidateMatchesOCRTokenFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() {
		f = new CandidateMatchesOCRTokenFeature("matches-ocr-token");
	}

	@Test
	public void testHandlesAnyOCR() {
		assertThat(f.handlesOCR(0, 2), is(true));
		assertThat(f.handlesOCR(1, 2), is(true));
	}

	@Test
	public void testMatchesOCRToken() {
		final OCRToken t = getCandidateToken(7, 1);
		assertThat(f.calculate(t, 0, 2), is(false));
		assertThat(f.calculate(t, 1, 2), is(true));
	}

	@Test
	public void testMatchesOCRTokenWithUpperCase() {
		final OCRToken t = getCandidateToken(0, 0);
		assertThat(f.calculate(t, 0, 2), is(false));
		assertThat(f.calculate(t, 1, 2), is(true));
	}
}
