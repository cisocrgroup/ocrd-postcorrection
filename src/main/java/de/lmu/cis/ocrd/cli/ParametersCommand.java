package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.BaseOCRTokenProfiler;
import de.lmu.cis.ocrd.ml.Workspace;
import de.lmu.cis.ocrd.profile.AdditionalLexicon;
import de.lmu.cis.ocrd.profile.FileProfiler;
import de.lmu.cis.ocrd.profile.LocalProfilerProcess;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

abstract class ParametersCommand implements Command {
    private final String name;
    protected Parameters parameters;
    protected Workspace workspace;

    ParametersCommand(String name) {
        this.name = name;
    }

    protected void init(CommandLineArguments config) throws Exception {
        this.parameters = config.mustGetParameter(Parameters.class);
        this.workspace = makeWorkspace(config, this.parameters);
    }

    private static Workspace makeWorkspace(CommandLineArguments config, Parameters parameters) throws Exception {
        if (parameters.isOcropus()) {
            return new de.lmu.cis.ocrd.ocropus.Workspace(parameters);
        }
        return new de.lmu.cis.ocrd.pagexml.Workspace(Paths.get(config.mustGetMETSFile()), parameters);
    }

    Profile getProfile(String ifg, AdditionalLexicon alex, int n) throws Exception {
        final Path cachedPath = parameters.getProfiler().getCachedPath(ifg, alex, n);
        if (cachedPath.toFile().exists()) {
            Logger.debug("opening cached profile {}", cachedPath.toString());
            return new FileProfiler(cachedPath).profile();
        }
        if (parameters.getProfiler().getPath().toString().endsWith(".json") ||
                parameters.getProfiler().getPath().toString().endsWith(".json.gz")) {
            Logger.debug("opening file profile {}", parameters.getProfiler().getPath().toString());
            return new FileProfiler(parameters.getProfiler().getPath()).profile();
        }
        Logger.debug("calculating profile");
        final Profile profile;
        profile = new BaseOCRTokenProfiler(
                workspace.getBaseOCRTokenReader(ifg),
                new LocalProfilerProcess(
                        parameters.getProfiler().getPath(),
                        parameters.getProfiler().getConfig(),
                        alex)
        ).profile();
        // cache the profile
        Logger.debug("caching profile to {}", cachedPath.toString());
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cachedPath.toFile())), StandardCharsets.UTF_8))) {
            w.write(profile.toJSON());
            w.write('\n');
        }
        return profile;
    }

    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public String getName() {
        return name;
    }
}
