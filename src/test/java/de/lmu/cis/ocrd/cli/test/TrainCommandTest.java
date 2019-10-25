package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.*;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.ml.LEProtocol;
import de.lmu.cis.ocrd.ml.Protocol;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TrainCommandTest {
	private final Path workspace = Paths.get("src/test/resources/workspace");
	private final Path tmp = Paths.get(workspace.toString(), "dump");
	private final String parameter = "src/test/resources/workspace/config.json";
	private final String mets = "src/test/resources/workspace/mets.xml";
	private final String inputFileGroupTrain = "OCR-D-PROFILED";
	private final String inputFileGroupEval = "OCR-D-EVAL";
	private final String outputFileGroup = "OCR-D-POST-CORRECTED";
	private final String logLevel = "INFO";
	private final String model = Paths.get(tmp.toString(), "model.zip").toString();

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
			FileUtils.deleteDirectory(Paths.get(workspace.toString(), outputFileGroup).toFile());
		} catch (Exception e) {
			// ignore
		}
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
		postCorrect();
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
			assertThat(exists(cmd.getParameter().leTraining.model, i), is(true));
			assertThat(exists(cmd.getParameter().leTraining.training, i), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.model, i), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.training, i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.model, i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.training, i), is(true));
		}
		// one cached profile for the single input file group
		cmd.getParameter().profiler.setAlex(new NoAdditionalLexicon());
		cmd.getParameter().profiler.setInputFileGroup(inputFileGroupTrain);
		assertThat(cmd.getParameter().profiler.getCachePath().toFile().exists(), is(true));
		assertThat(new File(cmd.getParameter().model).exists(), is(true));
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
			assertThat(exists(cmd.getParameter().leTraining.evaluation, i), is(true));
			assertThat(exists(cmd.getParameter().leTraining.lexicon, i), is(true));
			assertThat(exists(cmd.getParameter().leTraining.result, i), is(true));
		}
		// one cached profile for the single input file group
		cmd.getParameter().profiler.setAlex(new NoAdditionalLexicon());
		cmd.getParameter().profiler.setInputFileGroup(inputFileGroupTrain);
		assertThat(cmd.getParameter().profiler.getCachePath().toFile().exists(), is(true));
		cmd.getParameter().profiler.path = cmd.getParameter().profiler.getCachePath().toString();
		Profile profile = cmd.getParameter().profiler.profile();
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
			assertThat(exists(cmd.getParameter().rrTraining.evaluation.replace(".arff", "_no_dle.arff"), i), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.result, i), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.result.replace(".txt", "_no_dle.txt"), i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.evaluation, i), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.result, i), is(true));
			final Path al = AbstractMLCommand.tagPath(cmd.getParameter().leTraining.lexicon, i+1);
//			Logger.info("al: {}", al.toString());
//			Logger.info("pr: {}", cmd.getParameter().profiler.getCacheFilePath(inputFileGroupEval, Optional.of(al)));
			cmd.getParameter().profiler.setAlex(new NoAdditionalLexicon());
			cmd.getParameter().profiler.setInputFileGroup(inputFileGroupEval);
			assertThat(cmd.getParameter().profiler.getCachePath().toFile().exists(), is(true));
			cmd.getParameter().profiler.path = cmd.getParameter().profiler.getCachePath().toString();
			Profile profile = cmd.getParameter().profiler.profile();
			assertThat(profile, notNullValue());
		}
	}

	private void postCorrect() throws Exception {
		String[] args = {
				"-c", "post-correct",
				"--mets", mets,
				"--parameter", parameter,
				"-I", inputFileGroupEval,
				"-O", outputFileGroup,
				"--log-level", logLevel,
		};
		assertThat(Paths.get(model).toFile().exists(), is(true));
		CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
		PostCorrectionCommand cmd = new PostCorrectionCommand();
		cmd.execute(cla);
		final Path dir = Paths.get(workspace.toString(), outputFileGroup);
		assertThat(dir.toFile().exists(), is(true));
		assertThat(dir.toFile().isDirectory(), is(true));
		assertThat(numberOfFiles(dir), is(1));
		assertThat(Paths.get(model).toFile().exists(), is(true));
		assertThat(Paths.get(tmp.toString(), "le-protocol.json").toFile().exists(), is(true));
		checkReadProtocol(new LEProtocol(), Paths.get(tmp.toString(), "le-protocol.json"));
		assertThat(Paths.get(tmp.toString(), "dm-protocol.json").toFile().exists(), is(true));
		checkReadProtocol(new DMProtocol(new HashMap<>()), Paths.get(tmp.toString(), "dm-protocol.json"));
	}

	private void checkReadProtocol(Protocol protocol, Path path) throws Exception {
		try (InputStream is = new FileInputStream(path.toFile())) {
			protocol.read(is);
		}
	}

	private static boolean exists(String path, int i) {
		return AbstractMLCommand.tagPath(path, i+1).toFile().exists();
	}

	private static int numberOfFiles(Path dir) {
		final File[] files = dir.toFile().listFiles();
		return files == null ? 0 : files.length;
	}
}
