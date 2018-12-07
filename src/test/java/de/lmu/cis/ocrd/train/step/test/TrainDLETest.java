package de.lmu.cis.ocrd.train.step.test;

import de.lmu.cis.ocrd.train.step.Config;
import de.lmu.cis.ocrd.train.step.ModelDir;
import de.lmu.cis.ocrd.train.step.TmpDir;
import de.lmu.cis.ocrd.train.step.TrainDLE;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TrainDLETest {
	private ModelDir mdir;
	private TmpDir tdir;
	private Config config;

	@Before
	public void init() throws IOException {
		this.mdir = new ModelDir(
				Files.createTempDirectory("ocrd-cis-train-dle-test-model-"));
		this.tdir = new TmpDir(
				Files.createTempDirectory("ocrd-cis-train-dle-test-tmp-"));
		this.config = new Config();
		config.profiler = "/home/flo/devel/work/Profiler/build/bin/profiler";
		config.profilerLanguageDir = "/home/flo/data/profiler-backend";
		config.profilerLanguage = "deutsch";
		config.trigrams = "src/test/resources/nGrams.csv";
		config.dleFeatures = "src/test/resources/dle-features.json";
		config.rrFeatures = "src/test/resources/dle-features.json";
		config.trainingFiles = new ArrayList<String>();
		config.trainingFiles.add("src/test/resources/page.xml");
		config.evaluationFiles = new ArrayList<String>();
	}

	@After
	public void deinit() {
		tdir.remove();
		mdir.remove();
	}

	@Test
	public void testNumberOfOtherOCR() throws Exception {
		TrainDLE tdle = new TrainDLE("INFO", mdir, tdir, config);
		assertThat(tdle.getLM().getNumberOfOtherOCRs(), is(2));
	}
}
