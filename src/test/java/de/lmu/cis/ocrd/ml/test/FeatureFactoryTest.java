package de.lmu.cis.ocrd.ml.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.TokenLengthClassFeature;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
public class FeatureFactoryTest {
	private FeatureFactory featureFactory;

	@Before
	public void init() {
		featureFactory = new FeatureFactory().register(TestFeature.class);
	}

	@Test
	public void testWithValidName() throws Exception {
		final String json = "{\"name\": \"test\", \"type\": \"de.lmu.cis.ocrd.ml.test.FeatureFactoryTest$TestFeature\", \"short\": 3, \"medium\": 8, \"long\": 13}";
		JsonObject o = new Gson().fromJson(json, JsonObject.class);
		Optional<Feature> feature = featureFactory.create(o);
		assertThat(feature.isPresent(), is(true));
		assertThat(feature.get(), is(new TestFeature(3, 8, 13, "test")));
	}

	@Test
	public void testWithInvalidName() throws Exception {
		final String json = "{\"name\": \"test\", \"type\": \"invalid.package.InvalidFeature\", \"min\": 0, \"max\": 1}";
		JsonObject o = new Gson().fromJson(json, JsonObject.class);
		Optional<Feature> feature = featureFactory.create(o);
		assertThat(feature.isPresent(), is(false));
	}

	public static class TestFeature extends TokenLengthClassFeature {
		private static final long serialVersionUID = 3240781744100370146L;

		public TestFeature(JsonObject o, ArgumentFactory args) {
			super(o, args);
		}

		TestFeature(int shrt, int medium, int lng, String name) {
			super(name, shrt, medium, lng);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof TestFeature)) return false;
			TestFeature that = (TestFeature) o;
			return that.getSet().equals(this.getSet()) && that.getName().equals(this.getName());
		}
	}
}
