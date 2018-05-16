package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.archive.Entry;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.GTFeature;
import de.lmu.cis.ocrd.ml.features.ScreamCaseFeature;
import de.lmu.cis.ocrd.ml.features.WeirdCaseFeature;
import de.lmu.cis.ocrd.train.Configuration;
import de.lmu.cis.ocrd.train.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        assertThat(environment.getDynamicLexiconFeatureSet(), is(Paths.get( name, "dLex", "features.ser")));
    }

    @Test
    public void testDynamicLexiconTrainingFile() {
        assertThat(environment.getDynamicLexiconTrainingFile(1), is(Paths.get( name, "dLex", "1", "training.arff")));
    }

    @Test
    public void testConfigurationFile() {
        assertThat(environment.getConfigurationFile(), is(Paths.get( name, "resources", "configuration.json")));
    }

    @Test
    public void testSetupDirs() {
        assertThat(Files.exists(environment.fullPath(environment.getResourcesDirectory())), is(true));
        assertThat(Files.exists(environment.getPath()), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory())), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory(1))), is(true));
    }

    @Test
    public void testWithGT() throws IOException {
        final String gt = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.withGT(gt);
        assertThat(environment.fullPath(environment.getGT()), is(Paths.get(base, name, "resources", "1841-DieGrenzboten-abbyy-small.zip")));
        assertThat(Files.exists(environment.fullPath(environment.getGT())), is(true));
    }

    @Test
    public void testWithMasterOCR() throws IOException {
        final String masterOCR = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.withMasterOCR(masterOCR);
        assertThat(environment.fullPath(environment.getMasterOCR()), is(Paths.get(base, name, "resources", "1841-DieGrenzboten-abbyy-small.zip")));
        assertThat(Files.exists(environment.fullPath(environment.getMasterOCR())), is(true));
    }

    @Test
    public void testAddOtherOCR() throws IOException {
        final String otherOCR1 = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String otherOCR2 = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.addOtherOCR(otherOCR1).addOtherOCR(otherOCR2);
        assertThat(environment.getNumberOfOtherOCR(), is(2));
        assertThat(environment.fullPath(environment.getOtherOCR(0)), is(Paths.get(base, name, "resources", "1841-DieGrenzboten-abbyy-small.zip")));
        assertThat(environment.fullPath(environment.getOtherOCR(1)), is(Paths.get(base, name, "resources", "1841-DieGrenzboten-tesseract-small.zip")));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory(3))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory(4))), is(false));
        assertThat(Files.exists(environment.fullPath(environment.getOtherOCR(0))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getOtherOCR(1))), is(true));
    }

    @Test
    public void testSerializationOfDynamicLexiconFeatureSet() throws IOException, ClassNotFoundException {
        final FeatureSet fs = new FeatureSet()
                .add(new ScreamCaseFeature("x"))
                .add(new WeirdCaseFeature("y"))
                .add(new GTFeature());
        environment.withDynamicLexiconFeatureSet(fs);
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconFeatureSet())), is(true));
        final FeatureSet ofs = environment.loadDynamicLexiconFeatureSet();
        assertThat(ofs.size(), is(3));
        assertThat(ofs.get(0).getName(), is("x"));
        assertThat(ofs.get(1).getName(), is("y"));
        assertThat(ofs.get(2).getName(), is("GT"));
        assertThat((ofs.get(0) instanceof ScreamCaseFeature), is(true));
        assertThat((ofs.get(1) instanceof WeirdCaseFeature), is(true));
        assertThat((ofs.get(2) instanceof GTFeature), is(true));
    }

    @Test
    public void testLoadConfigurationFile() throws IOException {
        final String abbyy = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String tess = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        final String ocropus = "src/test/resources/1841-DieGrenzboten-ocropus-small.zip";
        environment.withGT(abbyy).withMasterOCR(tess).addOtherOCR(ocropus).withDebugTokenAlignment(true);
        environment.writeConfiguration();
        assertThat(Files.exists(environment.fullPath(environment.getConfigurationFile())), is(true));
        final Configuration configuration = environment.loadConfiguration();
        assertThat(configuration.gt, is(abbyy));
        assertThat(configuration.masterOCR, is(tess));
        assertThat(configuration.dynamicLexiconFeatureSet, is(environment.getDynamicLexiconFeatureSet().toString()));
        assertThat(configuration.otherOCR.length, is(1));
        assertThat(configuration.otherOCR[0], is(ocropus));
        assertThat(configuration.dynamicLexiconTrainingFiles.length, is(2));
        assertThat(configuration.dynamicLexiconTrainingFiles[0], is(environment.getDynamicLexiconTrainingFile(1).toString()));
        assertThat(configuration.dynamicLexiconTrainingFiles[1], is(environment.getDynamicLexiconTrainingFile(2).toString()));
        assertThat(configuration.debugTokenAlignment, is(environment.isDebugTokenAlignment()));
        assertThat(configuration.copyTrainingFiles, is(false));
        assertThat(configuration.configuration, is(environment.getConfigurationFile().toString()));
    }

    @Test
    public void testZIPTo() throws IOException {
        final String abbyy = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String tess = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        final String ocropus = "src/test/resources/1841-DieGrenzboten-ocropus-small.zip";
        environment.withCopyTrainingFiles(true).withGT(abbyy).withMasterOCR(tess).addOtherOCR(ocropus);
        try {
            environment.zipTo(Paths.get("src/test/resources/test.zip"));
            assertThat(Files.exists(Paths.get("src/test/resources/test.zip")), is(true));
        } finally {
            Files.delete(Paths.get("src/test/resources/test.zip"));
        }
    }

    @Test
    public void testZIPContent() throws Exception {
        final String abbyy = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String tess = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        final String ocropus = "src/test/resources/1841-DieGrenzboten-ocropus-small.zip";
        environment
                .withCopyTrainingFiles(true)
                .withGT(abbyy)
                .withMasterOCR(tess)
                .addOtherOCR(ocropus)
                .withDynamicLexiconFeatureSet(new FeatureSet().add(new GTFeature()));
        final Path zip = Paths.get("src","test","resources","test.zip");
        environment.zipTo(zip);
        assertThat(Files.exists(zip), is(true));
        try (ZipArchive ar = new ZipArchive(zip.toString())) {
            int n = 0;
            for (Entry e : ar) {
                n++;
            }
            // 3 (training files) + 1 (configuration file) + 1 (serialization file) = 5
            assertThat(n, is(5));
        } finally {
            Files.delete(zip);
        }
    }


    @After
    public void deInit() throws IOException {
        this.environment.remove();
        assertThat(Files.exists(this.environment.getPath()), is(false));
    }
}
