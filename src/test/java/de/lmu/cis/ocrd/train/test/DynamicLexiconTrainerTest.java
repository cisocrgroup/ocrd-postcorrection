package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.TokenCaseFeature;
import de.lmu.cis.ocrd.ml.features.TokenLengthFeature;
import de.lmu.cis.ocrd.train.DynamicLexiconTrainer;
import de.lmu.cis.ocrd.train.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DynamicLexiconTrainerTest extends TestBase {
    private Environment environment;
    private FeatureSet fs;
    private DynamicLexiconTrainer trainer;

    @Before
    public void init() throws IOException {
		this.environment = newEnvironment()
                .withCopyTrainingFiles(true)
                .withGT("src/test/resources/1841-DieGrenzboten-gt-small.zip")
                .withMasterOCR("src/test/resources/1841-DieGrenzboten-tesseract-small.zip")
                .addOtherOCR("src/test/resources/1841-DieGrenzboten-abbyy-small.zip")
                .addOtherOCR("src/test/resources/1841-DieGrenzboten-ocropus-small.zip")
                .withDebugTokenAlignment(true);
        this.fs = new FeatureSet()
                .add(new TokenLengthFeature(3, 8, 13, "TokenLength"))
                .add(new TokenCaseFeature("TokenCase"));
        trainer = new DynamicLexiconTrainer(environment, fs).withSplitFraction(2);
    }

    @Test
    public void testPrepare() throws Exception {
        trainer.prepare();
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingFile(1))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingFile(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingFile(3))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTestFile(1))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTestFile(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTestFile(3))), is(true));
    }

    @Test
    public void testTrain() throws Exception {
        trainer.prepare().train();
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconModel(1))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconModel(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconModel(3))), is(true));
    }

    @Test
    public void testEvaluation() throws Exception {
        trainer.prepare().train().evaluate();
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconEvaluationFile(1))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconEvaluationFile(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconEvaluationFile(3))), is(true));
	}

	@Test
	public void checkTestFileContent() throws Exception {
		trainer.prepare();
		assertThat(getNumberOfTestTokens(environment.fullPath(environment.getDynamicLexiconTestFile(1))), is(432));
		assertThat(getNumberOfTestTokens(environment.fullPath(environment.getDynamicLexiconTestFile(2))), is(432));
		assertThat(getNumberOfTestTokens(environment.fullPath(environment.getDynamicLexiconTestFile(3))), is(432));
	}

	@SuppressWarnings("unchecked")
	private static int getNumberOfTestTokens(Path path) throws IOException, ClassNotFoundException {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path.toFile()))) {
			return ((ArrayList<Token>) in.readObject()).size();
		}
	}

    @After
    public void deInit() throws Exception {
        this.environment.remove();
    }
}
