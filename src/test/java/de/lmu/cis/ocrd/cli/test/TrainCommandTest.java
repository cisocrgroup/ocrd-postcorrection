package de.lmu.cis.ocrd.cli.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.cli.CommandLineArguments;
import de.lmu.cis.ocrd.cli.EvaluateCommand;
import de.lmu.cis.ocrd.cli.PostCorrectionCommand;
import de.lmu.cis.ocrd.cli.TrainCommand;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.DMProtocol;
import de.lmu.cis.ocrd.ml.LEProtocol;
import de.lmu.cis.ocrd.ml.ModelZIP;
import de.lmu.cis.ocrd.ml.Protocol;
import de.lmu.cis.ocrd.pagexml.METS;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TrainCommandTest {
	private final Path workspace = Paths.get("src/test/resources/workspace");
	private Path tmp;
	private Parameters parameters;
	private final String mets = "src/test/resources/workspace/mets.xml";
	private final String inputFileGroupEval = "OCR-D-EVAL";
	private final String outputFileGroup = "OCR-D-POST-CORRECTED";
	private final String logLevel = "INFO";
	// private final String logLevel = "DEBUG"; // use this to enable debugging

	@Before
	public void init() throws IOException {
		tmp = Files.createTempDirectory("OCR-D-CIS-JAVA");
		try (Reader r = new FileReader(Paths.get("src/test/resources/workspace/config.json").toFile())) {
			parameters = new Gson().fromJson(r, Parameters.class);
		}
		parameters.setDir(tmp.toString());
		try {
			Files.createDirectory(tmp);
		} catch (FileAlreadyExistsException e) {
			// ignore
		}
		Files.copy(Paths.get(mets), Paths.get(tmp.toString(), "mets.xml"), REPLACE_EXISTING);
	}

	@After
	public void deinit() {
		try {
			Files.copy(Paths.get(tmp.toString(), "mets.xml"), Paths.get(mets), REPLACE_EXISTING);
		} catch (Exception e) {
			// ignore
		}
		if ("debug".equalsIgnoreCase(logLevel)) { // do not remove tmp dir if debugging
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
		String inputFileGroupTrain = "OCR-D-PROFILED";
		String[] args = {
				"-c", "train",
				"--mets", mets,
				"--parameter", new Gson().toJson(parameters),
				"-I", inputFileGroupTrain,
				"--log-level", logLevel,
		};
		CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
		TrainCommand cmd = new TrainCommand();
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
				"--parameter", new Gson().toJson(parameters),
				"-I", inputFileGroupEval,
				"-O", outputFileGroup,
				"--log-level", logLevel,
		};
		assertThat(parameters.getModel().toFile().exists(), is(true));
		for (int i = 0; i < 2; i++) {
			final String ofg = outputFileGroup + "-" + (i+1);
			args[9] = ofg;
			parameters.setNOCR(i+1);
			args[5] = new Gson().toJson(parameters); // set parameter as inline json string
            CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
			PostCorrectionCommand cmd = new PostCorrectionCommand();
			cmd.execute(cla);
			assertThat(cmd.getParameters().getNOCR(), is(parameters.getNOCR()));
			// check corrected files in output file group
			final Path dir = Paths.get(workspace.toString(), ofg);
			assertThat(dir.toFile().exists(), is(true));
			assertThat(dir.toFile().isDirectory(), is(true));
			assertThat(numberOfFiles(dir), is(1));
			assertThat(METS.open(Paths.get(mets)).findFileGrpFiles(ofg).size(), is(1));
			assertThat(Paths.get(METS.open(Paths.get(mets)).findFileGrpFiles(ofg).get(0).getFLocat().substring(7)).toFile().exists(), is(true));
			FileUtils.deleteDirectory(dir.toFile());
			assertThat(dir.toFile().exists(), is(false));
			assertThat(Paths.get(METS.open(Paths.get(mets)).findFileGrpFiles(ofg).get(0).getFLocat().substring(7)).toFile().exists(), is(false));
			// check protocols
            assertThat(cmd.getParameters().getLETraining().getProtocol(i+1, parameters.isRunLE()).toFile().exists(), is(true));
			checkReadProtocol(new LEProtocol(), cmd.getParameters().getLETraining().getProtocol(i+1, parameters.isRunLE()));
            assertThat(cmd.getParameters().getDMTraining().getProtocol(i+1, parameters.isRunLE()).toFile().exists(), is(true));
			checkReadProtocol(new DMProtocol(null), cmd.getParameters().getDMTraining().getProtocol(i+1, parameters.isRunLE()));
		}
	}

    private void eval() throws Exception {
        String[] args = {
                "-c", "eval",
                "--mets", mets,
                "--parameter", new Gson().toJson(parameters),
                "-I", inputFileGroupEval,
                "--log-level", logLevel,
        };
        for (int i = 0; i < 2; i++) {
            parameters.setNOCR(i+1);
            args[5] = new Gson().toJson(parameters); // set parameter as inline json string
            CommandLineArguments cla = CommandLineArguments.fromCommandLine(args);
            EvaluateCommand cmd = new EvaluateCommand();
            cmd.execute(cla);
            assertThat(cmd.getParameters().getDMTraining().getEvaluation(i+1).toFile().exists(), is(true));
        }
    }

    private void checkReadProtocol(Protocol protocol, Path path) throws Exception {
		try (InputStream is = new FileInputStream(path.toFile())) {
			protocol.read(is);
		}
	}

	private static int numberOfFiles(Path dir) {
		final File[] files = dir.toFile().listFiles();
		return files == null ? 0 : files.length;
	}
}
