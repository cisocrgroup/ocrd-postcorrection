package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.*;
import de.lmu.cis.ocrd.profile.FileProfiler;
import de.lmu.cis.ocrd.profile.Profile;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TrainCommandTest {
	private final Path tmp = Paths.get("src/test/resources/workspace/dump");
	private final String parameter = "src/test/resources/workspace/config.json";
	private final String mets = "src/test/resources/workspace/mets.xml";
	private final String inputFileGroupTrain = "OCR-D-PROFILED";
	private final String inputFileGroupEval = "OCR-D-EVAL";
	private final String logLevel = "DEBUG";

	private AbstractMLCommand.Parameter config;


	@Before
	public void init() throws IOException {
		try {
			Files.createDirectory(tmp);
		} catch (FileAlreadyExistsException e) {
			// ignore
		}
	}

	@After
	public void deinit() {
		try {
			FileUtils.deleteDirectory(tmp.toFile());
		} catch (Exception e) {
			// ignore
		}
	}

	@Test
	public void test() throws Exception {
		train();
		evalDLE();
		evalRRDM();
	}

	private void train() throws Exception {
		String[] args = {
				"-c", "train",
				"--mets", mets,
				"--parameter", parameter,
				"-I", inputFileGroupTrain,
				"--log-level", logLevel,
		};
		CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
		TrainCommand cmd = new TrainCommand();
		cmd.execute(cla);
		// 3 runs (dle, rr, dm), 2 files for each run with 2 OCRs
		for (int i = 0; i < 2; i++) {
			assertThat(exists(cmd.getParameter().dleTraining.model, i), is(true));
			assertThat(exists(cmd.getParameter().dleTraining.training, i), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.model, i), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.training, i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.model, i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.training, i), is(true));
		}
		// one cached profile for the single input file group
		assertThat(cmd.getParameter().profiler.getCacheFilePath(inputFileGroupTrain, Optional.empty()).toFile().exists(), is(true));
	}

	private void evalDLE() throws Exception {
		String[] args = {
			"-c", "eval-dle",
			"--mets", mets,
			"--parameter", parameter,
			"-I", inputFileGroupEval,
			"--log-level", logLevel,
		};
		CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
		EvaluateDLECommand cmd = new EvaluateDLECommand();
		cmd.execute(cla);
		// existing files (see above) + 2 lexicon, 2 eval and 2 result files
		for (int i = 0; i < 2; i++) {
			assertThat(exists(cmd.getParameter().dleTraining.evaluation, i), is(true));
			assertThat(exists(cmd.getParameter().dleTraining.dynamicLexicon, i), is(true));
			assertThat(exists(cmd.getParameter().dleTraining.result, i), is(true));
		}
		// one cached profile for the single input file group
		assertThat(cmd.getParameter().profiler.getCacheFilePath(inputFileGroupTrain, Optional.empty()).toFile().exists(), is(true));
		Profile profile = new FileProfiler(cmd.getParameter().profiler.getCacheFilePath(inputFileGroupTrain, Optional.empty())).profile();
		assertThat(profile, notNullValue());
	}

	private void evalRRDM() throws Exception {
		String[] args = {
				"-c", "eval-rrdm",
				"--mets", mets,
				"--parameter", parameter,
				"-I", inputFileGroupEval,
				"--log-level", logLevel,
		};
		CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
		EvaluateRRDMCommand cmd = new EvaluateRRDMCommand();
		cmd.execute(cla);
		// existing files (see above) + 4 eval and 4 result files (for each rr and dm training) and two profile cache files
		for (int i = 0; i < 2; i++) {
			assertThat(exists(cmd.getParameter().rrTraining.evaluation, i), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.result, i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.evaluation, i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.result, i), is(true));
			final Path al = AbstractMLCommand.tagPath(cmd.getParameter().dleTraining.dynamicLexicon, i+1);
//			Logger.info("al: {}", al.toString());
//			Logger.info("pr: {}", cmd.getParameter().profiler.getCacheFilePath(inputFileGroupEval, Optional.of(al)));
			assertThat(cmd.getParameter().profiler.getCacheFilePath(inputFileGroupEval, Optional.of(al)).toFile().exists(), is(true));
			Profile profile = new FileProfiler(cmd.getParameter().profiler.getCacheFilePath(inputFileGroupEval, Optional.of(al))).profile();
			assertThat(profile, notNullValue());
		}
	}

	private static boolean exists(String path, int i) {
		return AbstractMLCommand.tagPath(path, i+1).toFile().exists();
	}
}
