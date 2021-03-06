package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.AbstractConfidenceFeature;
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
				"MIN-historical-pattern-confidence");
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
		assertThat(min.calculate(getCandidateToken(3, 2), 0, 2),
				is(0.864349247184784));//0.374776831732323));//0.990718913181631));
	}

	@Test
	public void testMaxReturnsMaxHistoricalPatternConfidenceLongPattern() {
		assertThat(max.calculate(getCandidateToken(3, 2), 0, 2),
				is(0.864349247184784));
	}

	@Test
	public void testMinReturnsMinHistoricalPatternConfidenceEmptyPattern() {
		assertThat(min.calculate(getCandidateToken(3, 7), 0, 2),
				is(AbstractConfidenceFeature.MIN));
	}

	@Test
	public void testMaxReturnsMaxHistoricalPatternConfidenceEmptyPattern() {
		assertThat(max.calculate(getCandidateToken(3, 7), 0, 2), is(AbstractConfidenceFeature.MIN));
	}
}
