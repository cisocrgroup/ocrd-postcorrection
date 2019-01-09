package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.HighestRankedCandidateHistoricalPatternsDistanceFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HighestRankedCandidateHistoricalPatternsDistanceFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() throws Exception {
		f = new HighestRankedCandidateHistoricalPatternsDistanceFeature(
				"historical-patterns-distance");
	}

	@Test
	public void testHandlesOnlyMasterOCR() {
		assertThat(f.handlesOCR(0, 2), is(true));
		assertThat(f.handlesOCR(1, 2), is(false));
	}

	@Test
	public void testReturnsHistoricalPatternsDistance() {
		assertThat(f.calculate(getToken(3), 0, 2), is(0.0));
	}

	@Test
	public void testReturnsZeroIfNoCandidates() {
		assertThat(f.calculate(getToken(1), 0, 2), is(0.0));
	}
}
