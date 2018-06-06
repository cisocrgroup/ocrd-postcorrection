package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.CommandLineArguments;
import de.lmu.cis.ocrd.json.JSONUtil;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DataTest {
	private CommandLineArguments commandLineArguments;

    @Before
    public void init() throws IOException, ParseException {
		commandLineArguments = CommandLineArguments.fromCommandLine(
                new String[]{
                        "xxx", "-w", "w", "-O", "O", "-m", "M", "-I", "I", "-c", "c",
                        "-p", "src/test/resources/testConfiguration.json",
                }
        );
    }

    @Test
    public void test() {
		assertThat(commandLineArguments.getParameters().getProfiler().getExecutable(), is("/test/profiler/executable"));
		assertThat(commandLineArguments.getParameters().getProfiler().getLanguageDirectory(), is("/test/profiler/language/directory"));
		assertThat(commandLineArguments.getParameters().getProfiler().getLanguage(), is("/test/profiler/language"));
		assertThat(commandLineArguments.getParameters().getProfiler().getArguments().length, is(2));
		assertThat(commandLineArguments.getParameters().getProfiler().getArguments()[0], is("arg1"));
		assertThat(commandLineArguments.getParameters().getProfiler().getArguments()[1], is("arg2"));
		assertThat(commandLineArguments.getParameters().getLanguageModel().getCharacterTrigrams(), is("src/test/resources/nGrams.csv"));
		assertThat(commandLineArguments.getParameters().getDynamicLexiconTrainig().isDebugTrainingTokens(), is(true));
		assertThat(commandLineArguments.getParameters().getDynamicLexiconTrainig().isCopyTrainingFiles(), is(false));
		assertThat(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures().length, is(2));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "name").getAsString(), is("TokenLength"));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "type").getAsString(), is("de.lmu.cis.ocrd.ml.features.TokenLengthClassFeature"));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "short").getAsInt(), is(3));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "medium").getAsInt(), is(8));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "long").getAsInt(), is(13));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[1], "name").getAsString(), is("TokenCase"));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[1], "type").getAsString(), is("de.lmu.cis.ocrd.ml.features.TokenCaseClassFeature"));
    }
}
