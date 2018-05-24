package de.lmu.cis.ocrd.cli.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.cli.ConfigurationJSON;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.TokenLengthFeature;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultConfigurationTest {
	private ConfigurationJSON data;

	@Before
	public void init() throws IOException {
		try (InputStream is = new FileInputStream(Paths.get("src", "main", "resources", "defaultConfiguration.json").toFile())) {
			StringWriter out = new StringWriter();
			IOUtils.copy(is, out, Charset.forName("UTF-8"));
			data = new Gson().fromJson(out.toString(), ConfigurationJSON.class);
		}
	}

	@Test
	public void testCreateTokenLengthFeature() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		final Optional<Feature> feature = FeatureFactory.getDefault().create(data.getDynamicLexiconTrainig().getFeatures()[0]);
		assertThat(feature.isPresent(), is(true));
		assertThat(feature.get() instanceof TokenLengthFeature, is(true));
		//final FeatureSet fs = new FeatureFactory().createFeatureSet(data.)
	}
}
