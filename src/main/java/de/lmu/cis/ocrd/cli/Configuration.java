package de.lmu.cis.ocrd.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

class Configuration {
	private static final Option COMMAND = Option.builder("c").longOpt("command").desc("set CLI command (required)")
			.hasArg().required().build();
	private static final Option GROUPID = Option.builder("g").longOpt("group-id")
			.desc("GROUPID der Dateien, die verwendet werden sollen").hasArg().build();
	private static final Option INPUT_FILEGRP = Option.builder("I").longOpt("input-file-grp")
			.desc("fileGrp(s), die die Inputdateien enthalten").hasArgs().required().build();
	private static final Option LOG_LEVEL = Option.builder("l").longOpt("log-level")
			.desc("Loglevel [OFF ERROR WARN INFO DEBUG TRACE]").hasArg().build();
	private static final Option METS = Option.builder("m").longOpt("mets").desc("METS URL (required)").hasArg()
			.required().build();
	private static final Option OUTPUT = Option.builder("o").longOpt("output-mets")
			.desc("Pfad zur neu erstellten METS Datei").hasArg().build();
	private static final Option OUTPUT_FILEGRP = Option.builder("O").longOpt("output-file-grp")
			.desc("fileGrp(s), die die Ausgabedateien enthalten (required)").hasArgs().required().build();
	private static final Option PARAMETER = Option.builder("p").longOpt("parameter")
			.desc("URL der Parameterdatei in JSON Format").hasArg().build();
	private static final Option WORKDIR = Option.builder("w").longOpt("working-dir")
			.desc("Arbeitsverzeichnis (required").hasArg().required().build();

	public static Configuration defaultConfiguration() {
		Configuration c = new Configuration();
		c.logLevel = "INFO";
		return c;
	}

	public static Configuration fromCommandLine(CommandLine line) {
		Configuration c = defaultConfiguration();
		if (line.hasOption(LOG_LEVEL.getLongOpt())) {
			c.logLevel = line.getOptionValue(LOG_LEVEL.getLongOpt());
		}
		if (line.hasOption(COMMAND.getLongOpt())) {
			c.command = line.getOptionValue(COMMAND.getLongOpt());
		}
		c.args = line.getArgs();
		return c;
	}

	public static Configuration fromCommandLine(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(createOptions(), args);
		return Configuration.fromCommandLine(line);
	}

	private static Options createOptions() {
		return new Options().addOption(METS).addOption(WORKDIR).addOption(INPUT_FILEGRP).addOption(OUTPUT_FILEGRP)
				.addOption(GROUPID).addOption(PARAMETER).addOption(LOG_LEVEL).addOption(OUTPUT).addOption(COMMAND);
	}

	private static String notNull(String str) {
		return str == null ? new String() : str;
	}

	private static String[] notNull(String[] strs) {
		return strs == null ? new String[0] : strs;
	}

	private String[] inputFilegrp, outputFilegrp, args;

	private String logLevel, parameter, workdir, mets, output, command;

	// public String getArg(Option opt, String def) {
	// System.out.println("opt: " + opt.getLongOpt());
	// if (!line.hasOption(opt.getLongOpt())) {
	// return def;
	// }
	// String res = line.getOptionValue(opt.getLongOpt());
	// if (res == null) {
	// return def;
	// }
	// return res;
	// }

	public String[] getArgs() {
		return notNull(args);
	}

	public String getCommand() {
		return notNull(command);
	}
}
