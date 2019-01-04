package de.lmu.cis.ocrd.cli;

import org.apache.commons.cli.ParseException;

// java -cp target/ocrd-0.1-jar-with-dependencies.jar de.lmu.cis.ocrd.cli.Main -m x -w w -I w -O x -c align src/test/resources/1841-DieGrenzboten-abbyy.zip src/test/resources/1841-DieGrenzboten-ocropus.zip
public class Main {
	public static void main(String[] args) {
		try {
			System.setProperty("file.encoding","UTF-8");
//			System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
//			System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), false, "UTF-8"));
//			System.setIn(new BufferedReader(new FileInputStream(FileDescriptor.in, "UTF-8"));
			run(args);
		} catch (ParseException e) {
			e.printStackTrace(System.err);
			System.exit(2);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static CommandFactory makeCommandFactory() throws Exception {
		return new CommandFactory()
				.register(ProfilerCommand.class)
				.register(TrainCommand.class)
				.register(EvalCommand.class)
				.register(EvaluateDLECommand.class)
				.register(EvaluateRRDMCommand.class)
				.register(AlignCommand.class);
	}

	// Parses command line arguments and execute command.
	private static void run(String[] args) throws Exception {
		final CommandFactory commandFactory = makeCommandFactory();
		CommandLineArguments commandLineArguments = CommandLineArguments.fromCommandLine(args);
		commandFactory.get(commandLineArguments.getCommand()).execute(commandLineArguments);
	}
}
