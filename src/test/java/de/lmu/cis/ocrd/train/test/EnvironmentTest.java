package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.GTFeature;
import de.lmu.cis.ocrd.ml.features.ScreamCaseFeature;
import de.lmu.cis.ocrd.ml.features.WeirdCaseFeature;
import de.lmu.cis.ocrd.train.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
public class EnvironmentTest {

    private final static String base = "src/test/resources";
    private final static String name = "test-environment";
    private Environment environment;

    @Before
    public void init() throws IOException {
        this.environment = new Environment(base, name);
        assertThat(Files.exists(this.environment.getPath()), is(true));
        assertThat(Files.isDirectory(environment.getPath()), is(true));
    }

    @Test
    public void testName() {
        assertThat(environment.getName(), is(name));
    }

    @Test
    public void testPath() {
        assertThat(environment.getPath(), is(Paths.get(base, name)));
    }

    @Test
    public void testDynamicLexiconFeatureSetPath() {
        assertThat(environment.getDynamicLexiconFeatureSet(), is(Paths.get(base, name, "dLex", "features.ser")));
    }

    @Test
    public void testSetupDirs() {
        assertThat(Files.exists(environment.getResourcesDirectory()), is(true));
        assertThat(Files.exists(environment.getPath()), is(true));
        assertThat(Files.exists(environment.getDynamicLexiconTrainingDirectory()), is(true));
        assertThat(Files.exists(environment.getDynamicLexiconTrainingDirectory(1)), is(true));
    }

    @Test
    public void testWithGT() throws IOException {
        final String gt = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.withGT(gt);
        assertThat(environment.getGT().toString(), is(base + "/" + name + "/resources/1841-DieGrenzboten-abbyy-small.zip"));
        assertThat(Files.exists(environment.getGT()), is(true));
    }

    @Test
    public void testWithMasterOCR() throws IOException {
        final String masterOCR = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.withMasterOCR(masterOCR);
        assertThat(environment.getMasterOCR().toString(), is(base + "/" + name + "/resources/1841-DieGrenzboten-abbyy-small.zip"));
        assertThat(Files.exists(environment.getMasterOCR()), is(true));
    }

    @Test
    public void testAddOtherOCR() throws IOException {
        final String otherOCR1 = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String otherOCR2 = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.addOtherOCR(otherOCR1).addOtherOCR(otherOCR2);
        assertThat(environment.getNumberOfOtherOCR(), is(2));
        assertThat(environment.getOtherOCR(0).toString(), is(base + "/" + name + "/resources/1841-DieGrenzboten-abbyy-small.zip"));
        assertThat(environment.getOtherOCR(1).toString(), is(base + "/" + name + "/resources/1841-DieGrenzboten-tesseract-small.zip"));
        assertThat(Files.exists(environment.getDynamicLexiconTrainingDirectory(2)), is(true));
        assertThat(Files.exists(environment.getDynamicLexiconTrainingDirectory(3)), is(true));
        assertThat(Files.exists(environment.getDynamicLexiconTrainingDirectory(4)), is(false));
        assertThat(Files.exists(environment.getOtherOCR(0)), is(true));
        assertThat(Files.exists(environment.getOtherOCR(1)), is(true));
    }

    @Test
    public void testSerializeDynamicLexiconFeatureSet() throws IOException {
        FeatureSet fs = new FeatureSet().add(new ScreamCaseFeature("x")).add(new WeirdCaseFeature("y")).add(new GTFeature());
        environment.withDynamicLexiconFeatureSet(fs);
        assertThat(Files.exists(environment.getDynamicLexiconFeatureSet()), is(true));
    }

    @Test
    public void testDeSerializeDynamicLexiconFeatureSet() throws IOException, ClassNotFoundException {
        testSerializeDynamicLexiconFeatureSet();
        FeatureSet fs = environment.loadDynamicLexiconFeatureSet();
        assertThat(fs.size(), is(3));
        assertThat(fs.get(0).getName(), is("x"));
        assertThat(fs.get(1).getName(), is("y"));
        assertThat(fs.get(2).getName(), is("GT"));
        assertThat((fs.get(0) instanceof ScreamCaseFeature), is(true));
        assertThat((fs.get(1) instanceof WeirdCaseFeature), is(true));
        assertThat((fs.get(2) instanceof GTFeature), is(true));
    }

    @After
    public void deInit() throws IOException {
        this.environment.remove();
        assertThat(Files.exists(this.environment.getPath()), is(false));
    }
}
