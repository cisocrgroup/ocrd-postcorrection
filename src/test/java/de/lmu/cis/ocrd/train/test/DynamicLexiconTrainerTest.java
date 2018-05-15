package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.ScreamCaseFeature;
import de.lmu.cis.ocrd.ml.features.TitleCaseFeature;
import de.lmu.cis.ocrd.ml.features.WeirdCaseFeature;
import de.lmu.cis.ocrd.train.DynamicLexiconTrainer;
import de.lmu.cis.ocrd.train.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DynamicLexiconTrainerTest {
    private Environment environment;
    private FeatureSet fs;

    @Before
    public void init() throws IOException {
        this.environment = new Environment("src/test/resources/", "train-test")
                .withCopyTrainingFiles(true)
                .withGT("src/test/resources/1841-DieGrenzboten-gt-small.zip")
                .withMasterOCR("src/test/resources/1841-DieGrenzboten-tesseract-small.zip")
                .addOtherOCR("src/test/resources/1841-DieGrenzboten-abbyy-small.zip")
                .addOtherOCR("src/test/resources/1841-DieGrenzboten-ocropus-small.zip")
                .withDebugTokenAlignment(true);
        this.fs = new FeatureSet()
                .add(new WeirdCaseFeature("weird-case"))
                .add(new ScreamCaseFeature("scream-case"))
                .add(new TitleCaseFeature("title-case"));
    }

    @Test
    public void test() throws Exception {
        DynamicLexiconTrainer trainer = new DynamicLexiconTrainer(environment, fs).withSplitFraction(2);
        trainer.train();
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingFile(1))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingFile(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingFile(3))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconEvaluationFile(1))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconEvaluationFile(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconEvaluationFile(3))), is(true));
    }

    @After
    public void deInit() throws Exception {
        this.environment.remove();
    }
}
