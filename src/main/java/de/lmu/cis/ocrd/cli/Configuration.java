package de.lmu.cis.ocrd.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

class Configuration {
	public static final Option METS = Option.builder("m").longOpt("mets").desc("METS URL (required)").hasArg()
			.required().build();
	public static final Option WORKDIR = Option.builder("w").longOpt("working-dir").desc("Arbeitsverzeichnis (required")
			.hasArg().required().build();
	public static final Option INPUT_FILEGRP = Option.builder("I").longOpt("input-file-grp")
			.desc("fileGrp(s), die die Inputdateien enthalten").hasArgs().required().build();
	public static final Option OUTPUT_FILEGRP = Option.builder("O").longOpt("output-file-grp")
			.desc("fileGrp(s), die die Ausgabedateien enthalten (required)").hasArgs().required().build();
	static final Option GROUPID = Option.builder("g").longOpt("group-id")
			.desc("GROUPID der Dateien, die verwendet werden sollen").hasArg().build();
	static final Option PARAMETER = Option.builder("p").longOpt("parameter")
			.desc("URL der Parameterdatei in JSON Format").hasArg().build();
	static final Option LOG_LEVEL = Option.builder("l").longOpt("log-level")
			.desc("Loglevel [OFF ERROR WARN INFO DEBUG TRACE]").hasArg().build();
	static final Option OUTPUT = Option.builder("o").longOpt("output-mets").desc("Pfad zur neu erstellten METS Datei")
			.hasArg().build();

	public static Options createOptions() {
		return new Options().addOption(METS).addOption(WORKDIR).addOption(INPUT_FILEGRP).addOption(OUTPUT_FILEGRP)
				.addOption(GROUPID).addOption(PARAMETER).addOption(LOG_LEVEL).addOption(OUTPUT);
	}

	private final CommandLine line;

	public Configuration(CommandLine line) {
		this.line = line;
	}

	public CommandLine getCommandLine() {
		return this.line;
	}
}
