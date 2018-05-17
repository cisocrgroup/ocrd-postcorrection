package de.lmu.cis.ocrd.cli.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.cli.AsyncArgumentFactory;
import de.lmu.cis.ocrd.cli.ConfigurationJSON;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.profile.FileProfiler;
import de.lmu.cis.ocrd.profile.Profiler;
import de.lmu.cis.ocrd.train.Environment;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AsyncArgumentFactoryTest {
    private final static String base = "src/test/resources";
    private final static String name = "test-environment";
    private Environment environment;
    private ConfigurationJSON configuration;
    private ArgumentFactory factory;

    @Before
    public void init() throws Exception {
        environment = new Environment(base, name)
                .withGT(base + "/1841-DieGrenzboten-gt-small.zip")
                .withMasterOCR(base + "/1841-DieGrenzboten-abbyy-small.zip")
                .addOtherOCR(base + "/1841-DieGrenzboten-tesseract-small.zip")
                .addOtherOCR(base + "/1841-DieGrenzboten-ocropus-small.zip");
        try (InputStream is = new FileInputStream(new File(base + "/testConfiguration.json"))) {
             StringWriter out = new StringWriter();
             IOUtils.copy(is, out, Charset.forName("UTF-8"));
             configuration = new Gson().fromJson(out.toString(), ConfigurationJSON.class);
        }
        final Profiler profiler = new FileProfiler(Paths.get("src", "test", "resources", "profile-test.json"));
        factory = new AsyncArgumentFactory(configuration, environment, profiler);
    }

    @Test
    public void testMasterOCRUnigrams() {
        assertThat(factory.getMasterOCRUnigrams().getAbsolute("Deutschland"), is(3));
    }

    @Test
    public void testOtherOCR0Unigrams() {
        assertThat(factory.getOtherOCRUnigrams(0).getAbsolute("Deutschland"), is(4));
    }

    @Test
    public void testOtherOCR1Unigrams() {
        assertThat(factory.getOtherOCRUnigrams(1).getAbsolute("Deuiſchland"), is(1));
    }

    @Test
    public void testCharTrigrams() {
        assertThat(factory.getCharacterTrigrams().getAbsolute("abc"), is(20));
    }

    @Test
    public void testGetProfile() {
        final String lookup = "Vnheilfolles";
        assertThat(factory.getProfile().containsKey(lookup), is(true));
    }

    @After
    public void deInit() throws IOException {
        environment.remove();
    }
}
