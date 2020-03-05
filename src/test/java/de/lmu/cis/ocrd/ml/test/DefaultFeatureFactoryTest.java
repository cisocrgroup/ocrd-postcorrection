package de.lmu.cis.ocrd.ml.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.*;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
public class DefaultFeatureFactoryTest {
	private static Feature makeFeature(String json) throws Exception {
		final FeatureFactory featureFactory = FeatureFactory.getDefault()
				.withArgumentFactory(new EmptyArgumentFactory());
		return featureFactory.create(new Gson().fromJson(json, JsonObject.class));
	}

	@Test
	public void testMinOCRConfidenceFeature() throws Exception {
		final String json = "{\"type\":\"de.lmu.cis.ocrd.ml.features.MinOCRCharacterConfidenceFeature\",\"name\":\"MinOCRConfidence\",\"ocrIndex\":0}";
		final Feature feature = makeFeature(json);
		assertThat(feature, notNullValue());
		assertThat(feature.getName(), is("MinOCRConfidence"));
		assertThat(feature instanceof MinOCRCharacterConfidenceFeature, is(true));
	}

	@Test
	public void testMaxOCRConfidenceFeature() throws Exception {
		final String json = "{\"type\":\"de.lmu.cis.ocrd.ml.features.MaxOCRCharacterConfidenceFeature\",\"name\":\"MaxOCRConfidence\",\"ocrIndex\":0}";
		final Feature feature = makeFeature(json);
		assertThat(feature, notNullValue());
		assertThat(feature.getName(), is("MaxOCRConfidence"));
		assertThat(feature instanceof MaxOCRCharacterConfidenceFeature, is(true));
	}

	@Test
	public void testMinCharNGramsFeature() throws Exception {
		final String json = "{\"type\":\"de.lmu.cis.ocrd.ml.features.MinCharNGramsFeature\",\"name\":\"MinCharNGrams\"}";
		final Feature feature = makeFeature(json);
		assertThat(feature, notNullValue());
		assertThat(feature.getName(), is("MinCharNGrams"));
		assertThat(feature instanceof MinCharNGramsFeature, is(true));
	}

	@Test
	public void testMaxCharNGramsFeature() throws Exception {
		final String json = "{\"type\":\"de.lmu.cis.ocrd.ml.features.MaxCharNGramsFeature\",\"name\":\"MaxCharNGrams\"}";
		final Feature feature = makeFeature(json);
		assertThat(feature, notNullValue());
		assertThat(feature.getName(), is("MaxCharNGrams"));
		assertThat(feature instanceof MaxCharNGramsFeature, is(true));
	}

	@Test
	public void testHighestProfilerVoteWeightFeature() throws Exception {
		final String json = "{\"type\":\"de.lmu.cis.ocrd.ml.features.HighestRankedCandidateVoteWeightFeature\",\"name\":\"ProfilerHighestVoteWeight\"}";
		final Feature feature = makeFeature(json);
		assertThat(feature, notNullValue());
		assertThat(feature.getName(), is("ProfilerHighestVoteWeight"));
		assertThat(feature instanceof HighestRankedCandidateVoteWeightFeature, is(true));
	}

	@Test
	public void testDeactivateFeatureFalse() throws Exception {
		final String json = "{\"deactivate\":false,\"type\":\"de.lmu.cis.ocrd.ml.features.MaxCharNGramsFeature\",\"name\":\"MaxCharNGrams\"}";
		final Feature feature = makeFeature(json);
		assertThat(feature, notNullValue());
	}
}
