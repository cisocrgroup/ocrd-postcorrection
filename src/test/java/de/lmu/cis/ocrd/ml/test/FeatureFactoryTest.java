package de.lmu.cis.ocrd.ml.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
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
		Feature feature = featureFactory.create(o);
		assertThat(feature, notNullValue());
		assertThat(feature, is(new TestFeature(3, 8, 13, "test")));
	}

	@Test(expected = Exception.class)
	public void testWithInvalidName() throws Exception {
		final String json = "{\"name\": \"test\", \"type\": \"invalid.package.InvalidFeature\", \"MIN\": 0, \"max\": 1}";
		JsonObject o = new Gson().fromJson(json, JsonObject.class);
		Feature feature = featureFactory.create(o);
		assertThat(feature, notNullValue());
	}

	@Test
	public void testWithFeatureClassFilter() throws Exception {
		FeatureClassFilter ff = new FeatureClassFilter(Arrays.asList("a", "b"));
		final String json = "[{\"classes\":[\"a\"]},{\"classes\":[\"b\"]},{\"classes\":[\"A\",\"b\"]},{\"classes\":[\"B\",\"a\"]}]";
		JsonObject[] os = new Gson().fromJson(json, JsonObject[].class);
		FeatureSet fs = featureFactory.createFeatureSet(os, ff);
		assertThat(fs.size(), is(0));
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
