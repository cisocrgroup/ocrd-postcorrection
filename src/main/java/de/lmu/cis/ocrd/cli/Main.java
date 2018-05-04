package de.lmu.cis.ocrd.cli;

import org.apache.commons.cli.ParseException;

// java -cp target/ocrd-0.1-jar-with-dependencies.jar de.lmu.cis.ocrd.cli.Main -m x -w w -I w -O x -c align src/test/resources/1841-DieGrenzboten-abbyy.zip src/test/resources/1841-DieGrenzboten-ocropus.zip
public class Main {
	private static final CommandFactory commands = commands();

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

	private static CommandFactory commands() {
		return new CommandFactory().register("info", new InfoCommand())
				.register("train-dynamic-lexicon", new TrainDynamicDictionaryCommand())
				.register("env", new EnvironmentCommand())
                .register("profiler", new ProfilerCommand())
				.register("align", new AlignCommand())
				.register("tryout", new TryoutCommand());
	}

	// Parses command line arguments and execute command.
	private static void run(String[] args) throws Exception {
		Configuration configuration = Configuration.fromCommandLine(args);
		commands.get(configuration.getCommand()).execute(configuration);
	}
}
