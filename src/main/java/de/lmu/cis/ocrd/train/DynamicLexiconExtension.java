package de.lmu.cis.ocrd.train;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.lmu.cis.ocrd.cli.CommandLineArguments;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

class DynamicLexiconExtension {
	private final static Option PROFILE = Option
		.builder("p")
		.longOpt("profile")
		.desc("set path to profile")
		.required()
		.build();
	private final static Option DIR = Option
		.builder("d")
		.longOpt("dir")
		.desc("base train directory")
		.required()
		.build();
	private final static Option GT = Option
		.builder("g")
		.longOpt("gt")
		.desc("set name of ground-truth")
		.required()
		.build();
	private final static Option MASTER_OCR = Option
		.builder("m")
		.longOpt("master-ocr")
		.desc("set name of master-OCR")
		.required()
		.build();
	private final static Option OTHER_OCR = Option
		.builder("o")
		.longOpt("other" + "-ocr")
		.desc("add addional OCR")
		.numberOfArgs(Option.UNLIMITED_VALUES)
		.build();
	private final static Option LOG_LEVEL = Option
		.builder("l")
		.longOpt("log-level")
		.desc("set log level")
		.build();
	private final static Option FEATURES = Option
		.builder("f")
		.longOpt("features")
		.desc("set feature configuration file")
		.build();

	private static Options options() {
		return new Options()
			.addOption(PROFILE)
			.addOption(DIR)
			.addOption(GT)
			.addOption(MASTER_OCR)
			.addOption(OTHER_OCR)
			.addOption(LOG_LEVEL)
			.addOption(FEATURES);
	}

	private void run(String[] args) {
		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options(), args);
	}
}
