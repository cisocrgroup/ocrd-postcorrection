package de.lmu.cis.ocrd.ml.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.TokenLengthFeature;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeatureFactoryTest {
    private FeatureFactory featureFactory;

    public static class TestFeature extends TokenLengthFeature {
        public TestFeature(JsonObject o, ArgumentFactory args) {
            super(o, args);
        }

        public TestFeature(int min, int max, String name) {
            super(min, max, name);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof TestFeature)) return false;
            TestFeature that = (TestFeature) o;
            return that.getMin() == this.getMin() && that.getMax() == this.getMax() && that.getName().equals(this.getName());
        }
    }

    @Before
    public void init() {
        featureFactory = new FeatureFactory().register(TestFeature.class);
    }

    @Test
    public void testWithValidName() throws Exception {
        final String json = "{\"name\": \"test\", \"type\": \"de.lmu.cis.ocrd.ml.test.FeatureFactoryTest$TestFeature\", \"min\": 0, \"max\": 1}";
        JsonObject o = new Gson().fromJson(json, JsonObject.class);
        Optional<Feature> feature = featureFactory.create(o);
        assertThat(feature.isPresent(), is(true));
        assertThat(feature.get(), is(new TestFeature(0, 1, "test")));
    }

    @Test
    public void testWithInvalidName() throws Exception {
        final String json = "{\"name\": \"test\", \"type\": \"invalid.package.InvalidFeature\", \"min\": 0, \"max\": 1}";
        JsonObject o = new Gson().fromJson(json, JsonObject.class);
        Optional<Feature> feature = featureFactory.create(o);
        assertThat(feature.isPresent(), is(false));
    }
}
