package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CommandLineArguments {

    private static final Option COMMAND = Option.builder("c").longOpt("command").desc("set CLI command (required)")
            .hasArg().required().build();
    private static final Option GROUPID = Option.builder("g").longOpt("group-id")
            .desc("GROUPID der Dateien, die verwendet werden sollen").hasArg().build();
    private static final Option INPUT_FILEGRP = Option.builder("I").longOpt("input-file-grp")
            .desc("fileGrp(s), die die Inputdateien enthalten").hasArgs().build();
    private static final Option LOG_LEVEL = Option.builder("l").longOpt("log-level")
            .desc("Loglevel [OFF ERROR WARN INFO DEBUG TRACE]").hasArg().build();
    private static final Option METS = Option.builder("m").longOpt("mets").desc("METS URL").hasArg()
            .build();
    private static final Option OUTPUT = Option.builder("o").longOpt("output-mets")
            .desc("Pfad zur neu erstellten METS Datei").hasArg().build();
    private static final Option OUTPUT_FILEGRP = Option.builder("O").longOpt("output-file-grp")
            .desc("fileGrp(s), die die Ausgabedateien enthalten").hasArgs().build();
    private static final Option PARAMETER = Option.builder("p").longOpt("parameter")
            .desc("URL der Parameterdatei in JSON Format").hasArg().build();
    private static final Option WORKDIR = Option.builder("w").longOpt("working-dir")
            .desc("Arbeitsverzeichnis").hasArg().build();
    private String groupID, logLevel, parameter, workdir, mets, output, command, define;
    private String[] inputFilegrp, outputFilegrp, args;

    private static CommandLineArguments defaultConfiguration() {
        CommandLineArguments c = new CommandLineArguments();
        c.logLevel = "INFO";
        return c;
    }

    private static CommandLineArguments fromCommandLine(CommandLine line) {
        CommandLineArguments c = defaultConfiguration();
        if (isSet(line, LOG_LEVEL)) {
            c.logLevel = getArg(line, LOG_LEVEL);
        }
        if (isSet(line, COMMAND)) {
            c.command = getArg(line, COMMAND);
        }
        if (isSet(line, GROUPID)) {
            c.groupID = getArg(line, GROUPID);
        }
        if (isSet(line, INPUT_FILEGRP)) {
            c.inputFilegrp = getArg(line, INPUT_FILEGRP).split(",");
        }
        if (isSet(line, METS)) {
            c.mets = getArg(line, METS);
        }
        if (isSet(line, OUTPUT)) {
            c.output = getArg(line, OUTPUT);
        }
        if (isSet(line, OUTPUT_FILEGRP)) {
            c.outputFilegrp = getArgs(line, OUTPUT_FILEGRP);
        }
        if (isSet(line, PARAMETER)) {
            c.parameter = getArg(line, PARAMETER);
        }
        if (isSet(line, WORKDIR)) {
            c.workdir = getArg(line, WORKDIR);
        }
        c.args = line.getArgs();
        c.setupLogger();
        return c;
    }

    public void setCommand(Command c) {
        setupLogger("cis." + c.getClass().getSimpleName());
    }

    public static CommandLineArguments fromCommandLine(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(createOptions(), args);
        return CommandLineArguments.fromCommandLine(line);
    }

    private static Options createOptions() {
        return new Options().addOption(METS).addOption(WORKDIR).addOption(INPUT_FILEGRP).addOption(OUTPUT_FILEGRP)
                .addOption(GROUPID).addOption(PARAMETER).addOption(LOG_LEVEL).addOption(OUTPUT).addOption(COMMAND);
    }

    private static String getArg(CommandLine line, Option option) {
        return line.getOptionValue(option.getLongOpt());
    }

    private static String[] getArgs(CommandLine line, Option option) {
        return line.getOptionValues(option.getLongOpt());
    }

    private static boolean isSet(CommandLine line, Option option) {
        return line.hasOption(option.getLongOpt());
    }

    private static String notNull(String str) {
        return str == null ? "" : str;
    }

    private static String[] notNull(String[] strs) {
        return strs == null ? new String[0] : strs;
    }

    public String[] getArgs() {
        return notNull(args);
    }

    public String getCommand() {
        return notNull(command);
    }

    public String getGroupID() {
        return notNull(groupID);
    }

    public String[] getInputFileGrp() {
        return notNull(inputFilegrp);
    }

    String getLogLevel() {
        return notNull(logLevel);
    }

    public String getMETS() {
        return notNull(mets);
    }

    public String getOutput() {
        return notNull(output);
    }

    public String[] getOutputFileGrp() {
        return notNull(outputFilegrp);
    }

    private String getParameter() {
        return notNull(parameter);
    }

    private <T> T getParameterX(java.lang.reflect.Type typeOfT) throws IOException {
        try(Reader is = new FileReader(notNull(parameter))) {
            return new Gson().fromJson(is, typeOfT);
        }
    }

    <T> T mustGetParameter(java.lang.reflect.Type typeOfT) throws Exception {
        if (isMissing(parameter)) {
            throw new Exception("missing command line options: -p / --parameter");
        }
        final String optArg = getParameter();
        if (!isReadableFile(optArg) && optArg.length() > 0 && optArg.charAt(0) == '{') {
            Logger.debug("parsing parameters as inline json");
            return new Gson().fromJson(optArg, typeOfT);
        }
        try(Reader r = new FileReader(optArg)) {
            Logger.debug("reading parameters from file {}", optArg);
            return new Gson().fromJson(r, typeOfT);
        }
    }

    private static boolean isReadableFile(String p) {
        final File f = new File(p);
        return f.isFile() && f.exists() && f.canRead();
    }

    String mustGetMETSFile() throws Exception {
    	if (isMissing(mets)) {
    		throw new Exception("missing command line options: -m or --mets");
	    }
	    return mets;
    }

    String[] mustGetInputFileGroups() throws Exception {
        if (inputFilegrp == null || inputFilegrp.length == 0) {
            throw new Exception("missing command line options -I or " +
                    "--input-file-grp");
        }
        return inputFilegrp;
    }

    private String[] mustGetOutputFileGroups() throws Exception {
        if (outputFilegrp== null || outputFilegrp.length == 0) {
            throw new Exception("missing command line options -O or " +
                    "--output-file-grp");
        }
        return outputFilegrp;
    }

    String mustGetSingleInputFileGroup() throws Exception {
        String[] inputFileGroups = mustGetInputFileGroups();
        if (inputFileGroups.length != 1) {
            throw new Exception("only one input file group allowed");
        }
        return inputFileGroups[0];
    }

    String mustGetSingleOutputFileGroup() throws Exception {
        String[] outputFileGroups = mustGetOutputFileGroups();
        if (outputFileGroups.length != 1) {
            throw new Exception("only one output file group allowed");
        }
        return outputFileGroups[0];
    }

    private static boolean isMissing(String str) {
        return str == null || "".equals(str);
    }

    String getWorkDir() {
        return notNull(workdir);
    }

    private void setupLogger() {
        Configurator.currentConfig()
                .writer(new ConsoleWriter(System.err))
                .level(Level.valueOf(getLogLevel()))
                .formatPattern("{date:HH:mm:ss.S} {level} cis." + this.getClass().getSimpleName() + " - {message}")
                .activate();
        Logger.debug("current log level: {}", Logger.getLevel());
    }

    private void setupLogger(String name) {
        Configurator.currentConfig()
                .writer(new ConsoleWriter(System.err))
                .level(Level.valueOf(getLogLevel()))
                .formatPattern("{date:HH:mm:ss.S} {level} " + name + " - {message}")
                .activate();
        Logger.debug("command: {}", name);
    }

}
