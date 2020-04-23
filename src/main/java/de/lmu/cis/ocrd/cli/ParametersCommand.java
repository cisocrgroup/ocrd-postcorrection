package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.BaseOCRTokenProfiler;
import de.lmu.cis.ocrd.ml.Workspace;
import de.lmu.cis.ocrd.profile.AdditionalLexicon;
import de.lmu.cis.ocrd.profile.FileProfiler;
import de.lmu.cis.ocrd.profile.LocalProfilerProcess;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

abstract class ParametersCommand implements Command {
    private final String name;
    protected Parameters parameters;
    protected Workspace workspace;
    private boolean debug;

    ParametersCommand(String name) {
        this.name = name;
    }

    protected void init(CommandLineArguments config) throws Exception {
        this.parameters = config.mustGetParameter(Parameters.class);
        // overwrite nOCR from commandline
        if (config.maybeGetNOCR().isPresent()) {
            this.parameters.setNOCR(config.maybeGetNOCR().get());
        }
        this.workspace = makeWorkspace(config, this.parameters);
        this.debug = "DEBUG".equalsIgnoreCase(config.getLogLevel());
        setupDirs();
    }

    private static Workspace makeWorkspace(CommandLineArguments config, Parameters parameters) throws Exception {
        if (parameters.isOcropus()) {
            return new de.lmu.cis.ocrd.ocropus.Workspace(parameters);
        }
        return new de.lmu.cis.ocrd.pagexml.Workspace(Paths.get(config.mustGetMETSFile()), parameters);
    }

    private void setupDirs() {
        if (parameters.getDir().toFile().mkdirs()) {
            Logger.debug("created dir {}", parameters.getDir().toString());
        }
        if (parameters.getCacheDir().toFile().mkdirs()) {
            Logger.debug("created dir {}", parameters.getCacheDir().toString());
        }
    }

    boolean isDebug() {
        return debug;
    }

    Profile getProfile(String ifg, AdditionalLexicon alex, int n) throws Exception {
        if (parameters.getProfiler().isNoCache()) {
            return getProfile(ifg, alex);
        }
        final Path cachedPath = parameters.getProfiler().getCachedPath(parameters.getCacheDir(), ifg, alex.use(), n);
        if (cachedPath.toFile().exists()) {
            Logger.debug("opening cached profile {}", cachedPath.toString());
            return new FileProfiler(cachedPath).profile();
        }
        if (parameters.getProfiler().getPath().toString().endsWith(".json") ||
                parameters.getProfiler().getPath().toString().endsWith(".json.gz")) {
            Logger.debug("opening file profile {}", parameters.getProfiler().getPath().toString());
            return new FileProfiler(parameters.getProfiler().getPath()).profile();
        }
        final Profile profile = getProfile(ifg, alex);
        // cache the profile
        Logger.debug("caching profile to {}", cachedPath.toString());
        // input file group could be a directory (if ocropus is used). So build all path components for the cache file
        if (cachedPath.getParent().toFile().mkdirs()) {
            Logger.debug("created dir {}", cachedPath.getParent().toString());
        }
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cachedPath.toFile())), StandardCharsets.UTF_8))) {
            w.write(profile.toJSON());
            w.write('\n');
        } catch (IOException e) {
            Logger.warn("cannot create profiler cache file {}: {}", cachedPath.toString(), e.getMessage());
        }
        return profile;
    }

    private Profile getProfile(String ifg, AdditionalLexicon alex) throws Exception {
        Logger.debug("calculating profile for ifg={}", ifg);
        return new BaseOCRTokenProfiler(
                workspace.getBaseOCRTokenReader(ifg),
                new LocalProfilerProcess(
                        parameters.getProfiler().getPath(),
                        parameters.getProfiler().getConfig(),
                        alex)
        ).profile();
    }

    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public String getName() {
        return name;
    }
}
