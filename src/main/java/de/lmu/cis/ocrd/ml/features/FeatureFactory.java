package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.FeatureSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;

public class FeatureFactory {
    private final HashSet<String> features = new HashSet<>();
    private ArgumentFactory args;

    public static FeatureFactory getDefault() {
        return new FeatureFactory()
                .register(UnigramFeature.class)
				.register(TokenLengthClassFeature.class)
                .register(TokenLengthFeature.class)
                .register(SumOfMatchingAdditionalOCRsFeature.class)
				.register(TokenCaseClassFeature.class)
                .register(ProfilerHighestVoteWeightFeature.class)
				.register(MinOCRConfidenceFeature.class)
				.register(MaxOCRConfidenceFeature.class)
                .register(MinCharNGramsFeature.class)
				.register(ProfilerHistoricalPatternsDistance.class)
				.register(ProfilerOCRPatternsDistance.class)
				.register(LinePositionFeature.class)
				.register(LineOverlapWithMasterOCRFeature.class)
                .register(MaxCharNGramsFeature.class);
    }

    public FeatureFactory withArgumentFactory(ArgumentFactory args) {
        this.args = args;
        return this;
    }

    public Optional<Feature> create(JsonObject o) throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        final String type = JSONUtil.mustGet(o, "type").getAsString();
        if (!features.contains(type)) {
            return Optional.empty();
        }
        final Class<?> clazz = Class.forName(type);
        final Class<?>[] parameters = new Class[]{JsonObject.class, ArgumentFactory.class};
        final Constructor c = clazz.getConstructor(parameters);
        return Optional.of((Feature) c.newInstance(o, args));
    }

    public <F extends Feature> FeatureFactory register(Class<F> feature) {
		features.add(feature.getName());
        return this;
    }

    public FeatureSet createFeatureSet(JsonObject[] os) throws Exception {
        FeatureSet fs = new FeatureSet();
        for (JsonObject o : os) {
            Optional<Feature> feature = create(o);
            if (!feature.isPresent()) {
                throw new Exception("cannot create feature from: " + o.toString());
            }
            fs.add(feature.get());
        }
        return fs;
    }
}
