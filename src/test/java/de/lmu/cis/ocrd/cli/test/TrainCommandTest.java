package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.Command;
import de.lmu.cis.ocrd.cli.CommandLineArguments;
import de.lmu.cis.ocrd.cli.TrainCommand;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class TrainCommandTest {
	private final String tmp = "src/test/resources/workspace/dump";
	private CommandLineArguments args;

	@Before
	public void init() throws Exception {
		String[] args = {
				"-c", "train",
				"--mets", "src/test/resources/workspace/mets.xml",
				"--parameter", "src/test/resources/workspace/config.json",
				"-I", "OCR-D-PROFILED",
				"--log-level", "DEBUG",
		};
		this.args = CommandLineArguments.fromCommandLine(args);
		new File(tmp).mkdirs();
	}

	// @After
	// public void deinit() throws Exception {
	// 	FileUtils.deleteDirectory(new File(tmp));
	// }

	@Test
	public void test() throws Exception {
		Command cmd = new TrainCommand();
		cmd.execute(args);
	}
}
