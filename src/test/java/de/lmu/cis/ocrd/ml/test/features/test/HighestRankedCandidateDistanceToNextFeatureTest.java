package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.HighestRankedCandidateDistanceToNextFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HighestRankedCandidateDistanceToNextFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() {
		f = new HighestRankedCandidateDistanceToNextFeature(
				"vote-weight-distance-to-next");
	}

	@Test
	public void testHandlesOnlyMasterOCR() {
		assertThat(f.handlesOCR(0, 2), is(true));
		assertThat(f.handlesOCR(1, 2), is(false));
	}

	@Test
	public void testReturnsWeightDistanceBetweenCandidates() {
		assertThat(f.calculate(getToken(0), 0, 2), is(1.0 - 2.01571e-10));
	}

	@Test
	public void testReturnsWeightDistanceBetweenCandidates2() {
		assertThat(f.calculate(getToken(1), 0, 2), is( 0.999972 - 2.77931e-05));
	}
//
//	@Test
//	public void testReturnsOneIfNoCandidates() {
//		assertThat(f.calculate(getToken(1), 0, 2), is( 1.0));
//	}
}
