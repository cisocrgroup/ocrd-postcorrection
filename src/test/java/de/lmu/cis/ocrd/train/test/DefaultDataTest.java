package de.lmu.cis.ocrd.train.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.TokenLengthClassFeature;
import de.lmu.cis.ocrd.train.Configuration;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultDataTest {
	private Configuration data;

	@Before
	public void init() throws IOException {
		try (InputStream is = new FileInputStream(Paths.get("src", "main", "resources", "defaultConfiguration.json").toFile())) {
			StringWriter out = new StringWriter();
			IOUtils.copy(is, out, Charset.forName("UTF-8"));
			data = new Gson().fromJson(out.toString(), Configuration.class);
		}
	}

	@Test
	public void testCreateTokenLengthFeature() throws Exception {
		final Optional<Feature> feature = FeatureFactory.getDefault().create(data.getDynamicLexiconTrainig().getFeatures()[0]);
		assertThat(feature.isPresent(), is(true));
		assertThat(feature.get() instanceof TokenLengthClassFeature, is(true));
		//final FeatureSet fs = new FeatureFactory().createFeatureSet(data.)
	}
}
