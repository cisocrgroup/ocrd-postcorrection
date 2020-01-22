package de.lmu.cis.ocrd.cli.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.cli.*;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.ml.LEProtocol;
import de.lmu.cis.ocrd.ml.ModelZIP;
import de.lmu.cis.ocrd.ml.Protocol;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
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
	// private final String logLevel = "DEBUG"; // use this to enable debugging
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
		if ("debug".equalsIgnoreCase(logLevel)) { // do not remove while debugging
			return;
		}
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
		eval();
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
		NewTrainCommand cmd = new NewTrainCommand();
		cmd.execute(cla);
		// 3 runs (dle, rr, dm), 2 files for each run with 2 OCRs
		for (int i = 0; i < 2; i++) {
			assertThat(cmd.getParameters().getLETraining().getModel(i+1).toFile().exists(), is(true));
			assertThat(cmd.getParameters().getLETraining().getTraining(i+1).toFile().exists(), is(true));
			assertThat(cmd.getParameters().getRRTraining().getModel(i+1).toFile().exists(), is(true));
			assertThat(cmd.getParameters().getRRTraining().getTraining(i+1).toFile().exists(), is(true));
			assertThat(cmd.getParameters().getDMTraining().getModel(i+1).toFile().exists(), is(true));
			assertThat(cmd.getParameters().getDMTraining().getTraining(i+1).toFile().exists(), is(true));
		}

		// check model
		assertThat(cmd.getParameters().getModel().toFile().exists(), is(true));
		ModelZIP model = ModelZIP.open(cmd.getParameters().getModel());
		assertThat(model.getLEFeatureSet(), notNullValue());
		assertThat(model.getLEFeatureSet().size(), is(cmd.getParameters().getLETraining().getFeatures().size()));
		assertThat(model.getRRFeatureSet(), notNullValue());
		assertThat(model.getRRFeatureSet().size(), is(cmd.getParameters().getRRTraining().getFeatures().size()));
		assertThat(model.getDMFeatureSet(), notNullValue());
		assertThat(model.getDMFeatureSet().size(), is(cmd.getParameters().getDMTraining().getFeatures().size()));
		assertThat(checkInputStream(model.openLanguageModel()), is(true));
		for (int i = 0; i < 2; i++) {
			assertThat(checkInputStream(model.openLEModel(i)), is(true));
			assertThat(checkInputStream(model.openRRModel(i)), is(true));
			assertThat(checkInputStream(model.openDMModel(i)), is(true));
		}
	}

	private static boolean checkInputStream(InputStream is) throws IOException {
		is.close();
		return is != null;
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
			assertThat(exists(cmd.getParameter().leTraining.evaluation, i+1), is(true));
			assertThat(exists(cmd.getParameter().leTraining.lexicon, i+1), is(true));
			assertThat(exists(cmd.getParameter().leTraining.result, i+1), is(true));
		}
		// one cached profile for the single input file group
		cmd.getParameter().profiler.setAlex(new NoAdditionalLexicon());
		cmd.getParameter().profiler.setInputFileGroup(inputFileGroupTrain);
		// assertThat(cmd.getParameter().profiler.getCachePath().toFile().exists(), is(true));
		// cmd.getParameter().profiler.path = cmd.getParameter().profiler.getCachePath().toString();
		// Profile profile = cmd.getParameter().profiler.profile();
		// assertThat(profile, notNullValue());
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
			assertThat(exists(cmd.getParameter().rrTraining.evaluation, i+1), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.evaluation.replace(".arff", "_no_dle.arff"), i+1), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.result, i+1), is(true));
			assertThat(exists(cmd.getParameter().rrTraining.result.replace(".txt", "_no_dle.txt"), i+1), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.evaluation, i+1), is(true));
			assertThat(exists(cmd.getParameter().dmTraining.result, i+1), is(true));
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
		AbstractMLCommand.Parameter parameter;
		try (Reader r = new FileReader(Paths.get(this.parameter).toFile())) {
			parameter = new Gson().fromJson(r, AbstractMLCommand.Parameter.class);
		}
		for (int i = 0; i < 2; i++) {
			parameter.nOCR = i + 1;
			args[5] = new Gson().toJson(parameter); // set parameter as inline json string
            CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
			PostCorrectionCommand cmd = new PostCorrectionCommand();
			cmd.execute(cla);
			assertThat(cmd.getParameter().nOCR, is(parameter.nOCR));
			final Path dir = Paths.get(workspace.toString(), outputFileGroup);
			assertThat(dir.toFile().exists(), is(true));
			assertThat(dir.toFile().isDirectory(), is(true));
			assertThat(numberOfFiles(dir), is(1));
			assertThat(Paths.get(model).toFile().exists(), is(true));
            assertThat(exists(Paths.get(tmp.toString(), "le_protocol.json").toString(), i+1), is(true));
			checkReadProtocol(new LEProtocol(), Paths.get(tmp.toString(), "le_protocol.json"), i+1);
            assertThat(exists(Paths.get(tmp.toString(), "dm_protocol.json").toString(), i+1), is(true));
			checkReadProtocol(new DMProtocol(new HashMap<>()), Paths.get(tmp.toString(), "dm_protocol.json"), i+1);
		}
	}

    private void eval() throws Exception {
        String[] args = {
                "-c", "eval-dle",
                "--mets", mets,
                "--parameter", parameter,
                "-I", inputFileGroupEval,
                "--log-level", logLevel,
        };
        for (int i = 0; i < 2; i++) {
            Parameters parameters;
            try (Reader r = new FileReader(Paths.get(parameter).toFile())) {
                parameters = new Gson().fromJson(r, Parameters.class);
            }
            parameters.setNOCR(i+1);
            args[5] = new Gson().toJson(parameters); // set parameter as inline json string
            CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
            EvaluateCommand cmd = new EvaluateCommand();
            cmd.execute(cla);
        }
    }

    private void checkReadProtocol(Protocol protocol, Path path, int n) throws Exception {
		path = AbstractMLCommand.tagPath(path.toString(), n);
		try (InputStream is = new FileInputStream(path.toFile())) {
			protocol.read(is);
		}
	}

	private static boolean exists(String path, int n) {
		return AbstractMLCommand.tagPath(path, n).toFile().exists();
	}

	private static int numberOfFiles(Path dir) {
		final File[] files = dir.toFile().listFiles();
		return files == null ? 0 : files.length;
	}
}
