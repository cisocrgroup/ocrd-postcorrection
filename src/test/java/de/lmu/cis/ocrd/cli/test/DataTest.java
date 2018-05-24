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
		assertThat(commandLineArguments.getParameters().getLanguageModel().getCharacterTrigrams(), is("src/test/resources/nGrams.csv"));
		assertThat(commandLineArguments.getParameters().getDynamicLexiconTrainig().isDebugTrainingTokens(), is(true));
		assertThat(commandLineArguments.getParameters().getDynamicLexiconTrainig().isCopyTrainingFiles(), is(false));
		assertThat(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures().length, is(2));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "name").getAsString(), is("name1"));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "type").getAsString(), is("type1"));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "min").getAsInt(), is(0));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[0], "max").getAsInt(), is(1));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[1], "name").getAsString(), is("name2"));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[1], "type").getAsString(), is("type2"));
		assertThat(JSONUtil.mustGet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures()[1], "arg").getAsString(), is("some arg"));
    }
}
