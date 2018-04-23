package de.lmu.cis.ocrd.cli;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
	private static final CommandFactory commands = commands();
	private static final Options options = options();

	private static CommandFactory commands() {
		return new CommandFactory().register("info", new InfoCommand()).register("env", new EnvironmentCommand());
	}

	public static void main(String[] args) {
		try {
			run(args);
		} catch (ParseException e) {
			System.err.println("[error] " + e.getMessage());
			System.exit(2);
		} catch (Exception e) {
			System.err.println("[error] " + e.getMessage());
			System.exit(1);
		}
	}

	private static Options options() {
		return Configuration.createOptions().addOption(
				Option.builder("c").longOpt("command").hasArg().required().desc("sets command (required)").build());
	}

	// Parses command line arguments and execute command.
	private static void run(String[] args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		Configuration configuration = new Configuration(parser.parse(options, args));
		commands.get(configuration.getCommandLine().getOptionValue("command")).execute(configuration);
	}
}
