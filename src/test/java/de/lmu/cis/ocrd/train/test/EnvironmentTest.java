package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.archive.Entry;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.TokenCaseFeature;
import de.lmu.cis.ocrd.ml.features.TokenLengthFeature;
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

public class EnvironmentTest extends TestBase {

    private Environment environment;

    @Before
    public void init() throws IOException {
		this.environment = newEnvironment();
        assertThat(Files.exists(this.environment.getPath()), is(true));
        assertThat(Files.isDirectory(environment.getPath()), is(true));
    }

    @Test
    public void testName() {
		assertThat(environment.getName(), is(getName()));
    }

    @Test
    public void testPath() {
		assertThat(environment.getPath(), is(Paths.get(getPath(), getName())));
    }

    @Test
    public void testDynamicLexiconFeatureSetPath() {
		assertThat(environment.getDynamicLexiconFeatureSet(), is(Paths.get(getName(), "dLex", "features.ser")));
    }

    @Test
    public void testDynamicLexiconTrainingFile() {
		assertThat(environment.getDynamicLexiconTrainingFile(1), is(Paths.get(getName(), "dLex", "1", "training.arff")));
    }

    @Test
    public void testConfigurationFile() {
		assertThat(environment.getDataFile(), is(Paths.get(getName(), "resources", "data.json")));
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
		assertThat(environment.fullPath(environment.getGT()), is(Paths.get(getPath(), getName(), "resources", "1841-DieGrenzboten-abbyy-small.zip")));
        assertThat(Files.exists(environment.fullPath(environment.getGT())), is(true));
    }

    @Test
    public void testWithMasterOCR() throws IOException {
        final String masterOCR = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.withMasterOCR(masterOCR);
		assertThat(environment.fullPath(environment.getMasterOCR()), is(Paths.get(getPath(), getName(), "resources", "1841-DieGrenzboten-abbyy-small.zip")));
        assertThat(Files.exists(environment.fullPath(environment.getMasterOCR())), is(true));
    }

    @Test
    public void testAddOtherOCR() throws IOException {
        final String otherOCR1 = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String otherOCR2 = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        environment.withCopyTrainingFiles(true);
        environment.addOtherOCR(otherOCR1).addOtherOCR(otherOCR2);
        assertThat(environment.getNumberOfOtherOCR(), is(2));
		assertThat(environment.fullPath(environment.getOtherOCR(0)), is(Paths.get(getPath(), getName(), "resources", "1841-DieGrenzboten-abbyy-small.zip")));
		assertThat(environment.fullPath(environment.getOtherOCR(1)), is(Paths.get(getPath(), getName(), "resources", "1841-DieGrenzboten-tesseract-small.zip")));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory(2))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory(3))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconTrainingDirectory(4))), is(false));
        assertThat(Files.exists(environment.fullPath(environment.getOtherOCR(0))), is(true));
        assertThat(Files.exists(environment.fullPath(environment.getOtherOCR(1))), is(true));
    }

    @Test
    public void testSerializationOfDynamicLexiconFeatureSet() throws IOException, ClassNotFoundException {
        final FeatureSet fs = new FeatureSet()
                .add(new TokenCaseFeature("x"))
                .add(new TokenLengthFeature(3, 8, 13, "y"))
                .add(new DynamicLexiconGTFeature());
        environment.withDynamicLexiconFeatureSet(fs);
        assertThat(Files.exists(environment.fullPath(environment.getDynamicLexiconFeatureSet())), is(true));
        final FeatureSet ofs = environment.loadDynamicLexiconFeatureSet();
        assertThat(ofs.size(), is(3));
        assertThat(ofs.get(0).getName(), is("x"));
        assertThat(ofs.get(1).getName(), is("y"));
        assertThat(ofs.get(2).getName(), is("DynamicLexiconGT"));
        assertThat((ofs.get(0) instanceof TokenCaseFeature), is(true));
        assertThat((ofs.get(1) instanceof TokenLengthFeature), is(true));
        assertThat((ofs.get(2) instanceof DynamicLexiconGTFeature), is(true));
    }

    @Test
    public void testLoadConfigurationFile() throws IOException {
        final String abbyy = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String tess = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        final String ocropus = "src/test/resources/1841-DieGrenzboten-ocropus-small.zip";
        environment.withGT(abbyy).withMasterOCR(tess).addOtherOCR(ocropus).withDebugTokenAlignment(true);
        environment.writeData();
		assertThat(Files.exists(environment.fullPath(environment.getDataFile())), is(true));
        final Environment.Data data = environment.loadData();
        assertThat(data.gt, is(abbyy));
        assertThat(data.masterOCR, is(tess));
        assertThat(data.dynamicLexiconFeatureSet, is(environment.getDynamicLexiconFeatureSet().toString()));
        assertThat(data.otherOCR.length, is(1));
        assertThat(data.otherOCR[0], is(ocropus));
        assertThat(data.dynamicLexiconTrainingFiles.length, is(2));
        assertThat(data.dynamicLexiconTrainingFiles[0], is(environment.getDynamicLexiconTrainingFile(1).toString()));
        assertThat(data.dynamicLexiconTrainingFiles[1], is(environment.getDynamicLexiconTrainingFile(2).toString()));
        assertThat(data.dynamicLexiconTestFiles[0], is(environment.getDynamicLexiconTestFile(1).toString()));
        assertThat(data.dynamicLexiconTestFiles[1], is(environment.getDynamicLexiconTestFile(2).toString()));
        assertThat(data.dynamicLexiconModelFiles[0], is(environment.getDynamicLexiconModel(1).toString()));
        assertThat(data.dynamicLexiconModelFiles[1], is(environment.getDynamicLexiconModel(2).toString()));
        assertThat(data.dynamicLexiconTestFiles[0], is(environment.getDynamicLexiconTestFile(1).toString()));
        assertThat(data.dynamicLexiconTestFiles[1], is(environment.getDynamicLexiconTestFile(2).toString()));
        assertThat(data.dynamicLexiconEvaluationFiles[0], is(environment.getDynamicLexiconEvaluationFile(1).toString()));
        assertThat(data.dynamicLexiconEvaluationFiles[1], is(environment.getDynamicLexiconEvaluationFile(2).toString()));
        assertThat(data.debugTokenAlignment, is(environment.isDebugTokenAlignment()));
        assertThat(data.copyTrainingFiles, is(false));
        assertThat(data.data, is(environment.getDataFile().toString()));
    }

    @Test
    public void testZIPTo() throws IOException {
        final String abbyy = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
        final String tess = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
        final String ocropus = "src/test/resources/1841-DieGrenzboten-ocropus-small.zip";
        environment.withCopyTrainingFiles(true).withGT(abbyy).withMasterOCR(tess).addOtherOCR(ocropus);
		final Path zip = Paths.get(getPath(), "test.zip");
        try {
			environment.zipTo(zip);
			assertThat(Files.exists(zip), is(true));
		} finally {
			if (Files.exists(zip)) {
				Files.delete(zip);
			}
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
                .withDynamicLexiconFeatureSet(new FeatureSet().add(new DynamicLexiconGTFeature()));
		final Path zip = Paths.get(getPath(), "test.zip");
        environment.zipTo(zip);
        assertThat(Files.exists(zip), is(true));
        try (ZipArchive ar = new ZipArchive(zip.toString())) {
            int n = 0;
			for (Entry ignored : ar) {
                n++;
            }
            // 3 (training files) + 1 (data file) + 1 (serialization file) = 5
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
