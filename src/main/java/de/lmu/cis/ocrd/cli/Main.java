package de.lmu.cis.ocrd.cli;

import org.apache.commons.cli.ParseException;

// java -cp target/ocrd-0.1-jar-with-dependencies.jar de.lmu.cis.ocrd.cli.Main -m x -w w -I w -O x -c align src/test/resources/1841-DieGrenzboten-abbyy.zip src/test/resources/1841-DieGrenzboten-ocropus.zip
public class Main {
	public static void main(String[] args) {
		try {
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
				.register(CheckCorpusCommand.class)
				.register(TokenizerCommand.class)
				.register(EvaluateCommand.class)
				.register(PostCorrectionCommand.class)
				.register(TrainCommand.class)
				.register(AlignCommand.class);
	}

	// Parses command line arguments and execute command.
	private static void run(String[] args) throws Exception {
		final CommandFactory commandFactory = makeCommandFactory();
		CommandLineArguments commandLineArguments = CommandLineArguments.fromCommandLine(args);
		commandFactory.get(commandLineArguments.getCommand()).execute(commandLineArguments);
	}
}
