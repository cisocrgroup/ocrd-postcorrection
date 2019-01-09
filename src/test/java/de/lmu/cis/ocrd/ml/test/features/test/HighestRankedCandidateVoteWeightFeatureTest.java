package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.HighestRankedCandidateVoteWeightFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HighestRankedCandidateVoteWeightFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() throws Exception {
		f = new HighestRankedCandidateVoteWeightFeature("vote-weight-feature");
	}

	@Test
	public void testHandlesOnlyMasterOCR() {
		assertThat(f.handlesOCR(0, 2), is(true));
		assertThat(f.handlesOCR(1, 2), is(false));
	}

	@Test
	public void testReturnsVoteWeight() {
		assertThat(f.calculate(getToken(0), 0, 2), is(0.996182));
	}

	@Test
	public void testReturnsOneIfNoCandidates() {
		assertThat(f.calculate(getToken(1), 0, 2), is(1.0));
	}
}
