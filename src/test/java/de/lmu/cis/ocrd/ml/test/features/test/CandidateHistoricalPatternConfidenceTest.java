package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.CandidateMaxHistoricalPatternConfidenceFeature;
import de.lmu.cis.ocrd.ml.features.CandidateMinHistoricalPatternConfidenceFeature;
import de.lmu.cis.ocrd.ml.features.Feature;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CandidateHistoricalPatternConfidenceTest extends FeaturesTestBase {
	private Feature min, max;

	@Before
	public void initFeatures() {
		min = new CandidateMinHistoricalPatternConfidenceFeature(
				"min-historical-pattern-confidence");
		max = new CandidateMaxHistoricalPatternConfidenceFeature(
				"max-historical-pattern-confidence");
	}

	@Test
	public void testMinHandlesOnlyMasterOCR() {
		assertThat(min.handlesOCR(0, 2), is(true));
		assertThat(min.handlesOCR(2, 2), is(false));
	}

	@Test
	public void testMaxHandlesOnlyMasterOCR() {
		assertThat(min.handlesOCR(0, 2), is(true));
		assertThat(min.handlesOCR(2, 2), is(false));
	}

	@Test
	public void testMinReturnsMinHistoricalPatternConfidence() {
		assertThat(min.calculate(getCandidateToken(0, 0), 0, 2),
				is(0.736466494721585));
	}

	@Test
	public void testMaxReturnsMaxHistoricalPatternConfidence() {
		assertThat(max.calculate(getCandidateToken(0, 0), 0, 2),
				is(0.736466494721585));
	}

	@Test
	public void testMinReturnsMinHistoricalPatternConfidenceLongPattern() {
		assertThat(min.calculate(getCandidateToken(4, 0), 0, 2),
				is(0.990718913181631));
	}

	@Test
	public void testMaxReturnsMaxHistoricalPatternConfidenceLongPattern() {
		assertThat(max.calculate(getCandidateToken(4, 0), 0, 2),
				is(0.996771903318927));
	}

	@Test
	public void testMinReturnsMinHistoricalPatternConfidenceEmptyPattern() {
		assertThat(min.calculate(getCandidateToken(3, 7), 0, 2),
				is(0.0));
	}

	@Test
	public void testMaxReturnsMaxHistoricalPatternConfidenceEmptyPattern() {
		assertThat(max.calculate(getCandidateToken(3, 7), 0, 2),
				is(0.86665140812043));
	}
}
