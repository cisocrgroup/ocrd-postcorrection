package de.lmu.cis.ocrd.cli.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.cli.*;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.ml.LEProtocol;
import de.lmu.cis.ocrd.ml.ModelZIP;
import de.lmu.cis.ocrd.ml.Protocol;
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

	private void postCorrect() throws Exception {
		String[] args = {
				"-c", "post-correct",
				"--mets", mets,
				"--parameter", parameter,
				"-I", inputFileGroupEval,
				"-O", outputFileGroup,
				"--log-level", logLevel,
		};
		Parameters parameters;
		try (Reader r = new FileReader(Paths.get(this.parameter).toFile())) {
			parameters = new Gson().fromJson(r, Parameters.class);
		}
		assertThat(Paths.get(model).toFile().exists(), is(true));
		for (int i = 0; i < 2; i++) {
			parameters.setNOCR(i+1);
			System.out.printf("%d\n", parameters.getNOCR());
			args[5] = new Gson().toJson(parameters); // set parameter as inline json string
            CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
			NewPostCorrectionCommand cmd = new NewPostCorrectionCommand();
			cmd.execute(cla);
			System.out.printf("%d - %d\n", cmd.getParameters().getNOCR(), parameters.getNOCR());
			assertThat(cmd.getParameters().getNOCR(), is(parameters.getNOCR()));
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
                "-c", "eval",
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
            NewEvaluateCommand cmd = new NewEvaluateCommand();
            cmd.execute(cla);
            assertThat(cmd.getParameters().getDMTraining().getEvaluation(i+1).toFile().exists(), is(true));
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
