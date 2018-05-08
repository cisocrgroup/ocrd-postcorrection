package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.Feature;
import de.lmu.cis.ocrd.ml.FeatureSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;

public class FeatureFactory {
    private static final Class[] constructorParams = new Class[]{JsonObject.class, ArgumentFactory.class};
    private final HashSet<String> features = new HashSet<>();
    private ArgumentFactory args;

    public static FeatureFactory getDefault() {
        return new FeatureFactory()
                .register(TokenLengthFeature.class)
                .register(UnigramFeature.class);
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
        Class clazz = Class.forName(type);
        Constructor c = clazz.getConstructor(constructorParams);
        return Optional.ofNullable((Feature)c.newInstance(o, args));
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
