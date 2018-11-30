package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.train.Configuration;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;

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
    private static final Option DEFINE = Option.builder("D").longOpt("define").hasArgs().
            desc("set values using inline json").build();
    private Configuration data;
    private String groupID, logLevel, parameter, workdir, mets, output, command, define;
    private String[] inputFilegrp, outputFilegrp, args;

    private static CommandLineArguments defaultConfiguration() {
        CommandLineArguments c = new CommandLineArguments();
        c.logLevel = "INFO";
        return c;
    }

    private static CommandLineArguments fromCommandLine(CommandLine line) throws IOException {
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
            c.inputFilegrp = getArgs(line, INPUT_FILEGRP);
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
        if (isSet(line, DEFINE)) {
            c.define = getArg(line, DEFINE);
        }
        c.args = line.getArgs();
        c.setupLogger();
        c.setupJSON();
        return c;
    }

    public static CommandLineArguments fromCommandLine(String[] args)
            throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(createOptions(), args);
        return CommandLineArguments.fromCommandLine(line);
    }

    private static Options createOptions() {
        return new Options().addOption(METS).addOption(WORKDIR).addOption(INPUT_FILEGRP).addOption(OUTPUT_FILEGRP)
                .addOption(GROUPID).addOption(PARAMETER).addOption(LOG_LEVEL).addOption(OUTPUT).addOption(COMMAND)
                .addOption(DEFINE);
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

    public String getLogLevel() {
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

    public String getParameter() {
        return notNull(parameter);
    }

    public Configuration getParameters() {
        assert (data != null);
        return data;
    }

    public <T> T getDefine(java.lang.reflect.Type typeOfT) {
        return new Gson().fromJson(notNull(define), typeOfT);
    }

    public <T> T getParameterX(java.lang.reflect.Type typeOfT) throws FileNotFoundException {
        Reader is = new FileReader(notNull(parameter));
        return new Gson().fromJson(is, typeOfT);
    }

    public <T> T mustGetParameter(java.lang.reflect.Type typeOfT) throws Exception {
        if (isSet(define)) {
            return getDefine(typeOfT);
        } else if (isSet(parameter)) {
            return getParameterX(typeOfT);
        }
        throw new Exception("no parameter defined");
    }

    private static boolean isSet(String str) {
        return str != null && !"".equals(str);
    }

    String getWorkDir() {
        return notNull(workdir);
    }

    private void setupJSON() throws IOException {
        data = Configuration.getDefault();
        if (parameter == null) {
            return;
        }
        Logger.debug("reading data {}", parameter);
        try (InputStream is = new FileInputStream(new File(parameter))) {
            StringWriter out = new StringWriter();
            IOUtils.copy(is, out, Charset.forName("UTF-8"));
            data = Configuration.fromJSON(out.toString());
        }
    }

    private void setupLogger() {
        Configurator.currentConfig().level(Level.valueOf(getLogLevel())).activate();
        Logger.debug("current log level: {}", Logger.getLevel());
    }
}
