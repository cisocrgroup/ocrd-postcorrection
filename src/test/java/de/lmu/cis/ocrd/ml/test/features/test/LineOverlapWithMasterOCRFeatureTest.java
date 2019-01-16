package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.LineOverlapWithMasterOCRFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class LineOverlapWithMasterOCRFeatureTest extends FeaturesTestBase {
	private Feature f;

	@Before
	public void initFeature() {
		f = new LineOverlapWithMasterOCRFeature("line-overlap");
	}

	@Test
	public void testHandlesEveryOtherOCR() {
		assertThat(f.handlesOCR(0, 2), is(false));
		assertThat(f.handlesOCR(1, 2), is(true));
		assertThat(f.handlesOCR(2, 3), is(true));
	}

	@Test
	public void testCalculatesLineOverlapWithMasterOCR() {
		final double want = 0.9090909090909091;
		Object got = f.calculate(getToken(0), 1, 2);
		assertThat(got, is(want));
		got = f.calculate(getToken(1), 1, 2);
		assertThat(got, is(want));
		got = f.calculate(getToken(2), 1, 2);
		assertThat(got, is(want));

		// this is the first token of the next line; make sure that the
		// values are updated.
		got = f.calculate(getToken(3), 1, 2);
		assertThat(got, not(want));
	}
}
