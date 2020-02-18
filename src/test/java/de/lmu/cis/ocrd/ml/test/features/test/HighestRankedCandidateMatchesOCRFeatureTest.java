package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.HighestRankedCandidateMatchesOCRFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HighestRankedCandidateMatchesOCRFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() throws Exception {
		f = new HighestRankedCandidateMatchesOCRFeature(
				"candidate-matches-ocr");
	}

	@Test
	public void testHandlesAnyOCR() {
		assertThat(f.handlesOCR(0, 2), is(true));
		assertThat(f.handlesOCR(1, 2), is(true));
	}

	@Test
	public void testReturnsTrueIfMatches() {
		assertThat(f.calculate(getToken(0), 1, 2), is(true));
	}

	@Test
	public void testReturnsFalseIfNotMatches() {
		assertThat(f.calculate(getToken(0), 0, 2), is(false));
	}

	@Test
	public void testReturnsFalseIfNoCandidates() {
		assertThat(f.calculate(getToken(1), 0, 2), is(false));
	}
}
