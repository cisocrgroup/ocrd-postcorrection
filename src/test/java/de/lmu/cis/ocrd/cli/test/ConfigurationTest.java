package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.Configuration;
import de.lmu.cis.ocrd.ml.features.JSONUtil;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationTest {
    private Configuration configuration;

    @Before
    public void init() throws IOException, ParseException {
        configuration = Configuration.fromCommandLine(
                new String[]{
                        "xxx", "-w", "w", "-O", "O", "-m", "M", "-I", "I", "-c", "c",
                        "-p", "src/test/resources/testConfiguration.json",
                }
        );
    }

    @Test
    public void test() {
        assertThat(configuration.getParameters().getProfiler().getExecutable(), is("/test/profiler/executable"));
        assertThat(configuration.getParameters().getProfiler().getLanguageDirectory(), is("/test/profiler/language/directory"));
        assertThat(configuration.getParameters().getProfiler().getLanguage(), is("/test/profiler/language"));
        assertThat(configuration.getParameters().getLanguageModel().getCharacterTrigrams(), is("/test/language/model/character/trigrams"));
        assertThat(configuration.getParameters().getDynamicLexiconFeatures().length, is(2));
        assertThat(JSONUtil.mustGet(configuration.getParameters().getDynamicLexiconFeatures()[0], "name").getAsString(), is("name1"));
        assertThat(JSONUtil.mustGet(configuration.getParameters().getDynamicLexiconFeatures()[0], "type").getAsString(), is("type1"));
        assertThat(JSONUtil.mustGet(configuration.getParameters().getDynamicLexiconFeatures()[0], "min").getAsInt(), is(0));
        assertThat(JSONUtil.mustGet(configuration.getParameters().getDynamicLexiconFeatures()[0], "max").getAsInt(), is(1));
        assertThat(JSONUtil.mustGet(configuration.getParameters().getDynamicLexiconFeatures()[1], "name").getAsString(), is("name2"));
        assertThat(JSONUtil.mustGet(configuration.getParameters().getDynamicLexiconFeatures()[1], "type").getAsString(), is("type2"));
        assertThat(JSONUtil.mustGet(configuration.getParameters().getDynamicLexiconFeatures()[1], "arg").getAsString(), is("some arg"));
    }
}
