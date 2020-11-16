package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.util.JSON;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class FeatureFactory {
	private final HashSet<String> features = new HashSet<>();
	private ArgumentFactory args;

	public static FeatureFactory getDefault() {
		return new FeatureFactory()
				.register(UnigramFeature.class)
				.register(CandidateUnigramFeature.class)
				.register(CandidateLengthClassFeature.class)
				.register(CandidateLengthFeature.class)
				.register(CandidateMaxCharNGramsFeature.class)
				.register(CandidateMaxHistoricalPatternConfidenceFeature.class)
				.register(CandidateMinHistoricalPatternConfidenceFeature.class)
				.register(CandidateMinCharNGramsFeature.class)
				.register(CandidateCaseClassFeature.class)
				.register(CandidateVoteWeightFeature.class)
				.register(CandidateHistoricalPatternsDistanceFeature.class)
				.register(CandidateOCRPatternsDistanceFeature.class)
				.register(CandidateMatchingOCRsFeature.class)
				.register(CandidateMatchesOCRTokenFeature.class)
				.register(TokenLengthClassFeature.class)
				.register(TokenLengthFeature.class)
				.register(MatchingOCRTokensFeature.class)
				.register(TokenCaseClassFeature.class)
				.register(MinOCRCharacterConfidenceFeature.class)
				.register(MaxOCRCharacterConfidenceFeature.class)
				.register(MinCharNGramsFeature.class)
				.register(HighestRankedCandidateVoteWeightFeature.class)
				.register(HighestRankedCandidateHistoricalPatternsDistanceFeature.class)
				.register(HighestRankedCandidateOCRPatternsDistanceFeature.class)
				.register(HighestRankedCandidateMatchesOCRFeature.class)
				.register(HighestRankedCandidateDistanceToNextFeature.class)
				.register(LinePositionFeature.class)
				.register(LineOverlapWithMasterOCRFeature.class)
				.register(OCRWordConfidenceFeature.class)
				.register(LevenshteinDistanceFeature.class)
				.register(CorrectionPlaceFeature.class)
				.register(DMBestRankFeature.class)
				.register(DMDifferenceToNextRankFeature.class)
				.register(AverageOCRCharacterConfidenceFeature.class)
				.register(MaxCharNGramsFeature.class)
				.register(CandidateIsAlternativeFeature.class)
				.register(CandidateAlternativeConfidenceFeature.class);
	}

	public FeatureFactory withArgumentFactory(ArgumentFactory args) {
		this.args = args;
		return this;
	}

	public Feature create(JsonObject o) throws Exception {
		final String type = JSON.mustGet(o, "type").getAsString();
		if (!features.contains(type)) {
			throw new Exception("cannot create feature: unknown type: " + type);
		}
		final Class<?> clazz = Class.forName(type);
		final Class<?>[] parameters = new Class[] { JsonObject.class, ArgumentFactory.class };
		final Constructor<?> c = clazz.getConstructor(parameters);
		return (Feature) c.newInstance(o, args);
	}

	public <F extends Feature> FeatureFactory register(Class<F> feature) {
		features.add(feature.getName());
		return this;
	}

	public FeatureSet createFeatureSet(JsonObject[] os, FeatureClassFilter ff) throws Exception {
		return createFeatureSet(Arrays.asList(os), ff);
	}

	public FeatureSet createFeatureSet(List<JsonObject> os, FeatureClassFilter ff) throws Exception {
		FeatureSet fs = new FeatureSet();
		for (JsonObject o : os) {
			if (ff.filter(o)) {
				continue;
			}
			fs.add(create(o));
		}
		return fs;
	}
}
