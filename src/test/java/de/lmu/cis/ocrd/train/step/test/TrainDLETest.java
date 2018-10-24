package de.lmu.cis.ocrd.train.step.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.lmu.cis.ocrd.train.step.TrainDLE;

public class TrainDLETest {
	private Path dir;

	@Before
	public void init() throws IOException {
		this.dir = Files.createTempDirectory("ocrd-cis-train-dle-test-");
	}

	@After
	public void deinit() {
		dir.toFile().delete();
	}

	@Test
	public void testOneFile() throws Exception {
		TrainDLE tdle = new TrainDLE(
				new String[] { "DEBUG", "y", "z", "ab", "cd", "ef" });
		assertThat(tdle.getFiles().size(), is(1));
		assertThat(tdle.getFiles().get(0).toString(), is("ef"));
	}

	@Test
	public void testTwoFiles() throws Exception {
		TrainDLE tdle = new TrainDLE(
				new String[] { "DEBUG", "y", "z", "ab", "cd", "ab", "cd" });
		assertThat(tdle.getFiles().size(), is(2));
		assertThat(tdle.getFiles().get(0).toString(), is("ab"));
		assertThat(tdle.getFiles().get(1).toString(), is("cd"));
	}

	@Test
	public void testNumberOfOtherOCR() throws Exception {
		TrainDLE tdle = new TrainDLE(
				new String[] { "DEBUG", "src/test/resources/dle-features.json",
						"src/test/resources/profile-test.json",
						"src/test/resources/nGrams.csv", dir.toString(),
						"src/test/resources/page.xml" });
		assertThat(tdle.getLM().getNumberOfOtherOCRs(), is(2));
	}

	@Test
	public void testRun() throws Exception {
		TrainDLE tdle = new TrainDLE(
				new String[] { "DEBUG", "src/test/resources/dle-features.json",
						"src/test/resources/profile-test.json",
						"src/test/resources/nGrams.csv", dir.toString(),
						"src/test/resources/page.xml" });
		tdle.run();
		// arffs
		assertThat(tdle.getTrain(0).toFile().exists(), is(false));
		assertThat(tdle.getTrain(1).toFile().exists(), is(true));
		assertThat(tdle.getTrain(2).toFile().exists(), is(true));
		assertThat(tdle.getTrain(3).toFile().exists(), is(true));
		assertThat(tdle.getTrain(4).toFile().exists(), is(false));
		// models
		assertThat(tdle.getModel(0).toFile().exists(), is(false));
		assertThat(tdle.getModel(1).toFile().exists(), is(true));
		assertThat(tdle.getModel(2).toFile().exists(), is(true));
		assertThat(tdle.getModel(3).toFile().exists(), is(true));
		assertThat(tdle.getModel(4).toFile().exists(), is(false));
	}
}
