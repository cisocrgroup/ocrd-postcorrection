package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.profile.*;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

public class METSFileGroupProfiler implements Profiler {
    private final Parameters parameters;
    private final WordReader wordReader;
    private final AdditionalLexicon alex;
    private final int n;
    private final String ifg;

    public METSFileGroupProfiler(Parameters parameters, WordReader wordReader, String ifg, AdditionalLexicon alex, int n) {
        this.parameters = parameters;
        this.wordReader = wordReader;
        this.ifg = ifg;
        this.alex = alex;
        this.n = n;
    }

    @Override
    public Profile profile() throws Exception {
        final Path cachedPath = parameters.getProfiler().getCachedPath(ifg, alex, n);
        if (cachedPath.toFile().exists()) {
            return new FileProfiler(cachedPath).profile();
        }
        if (parameters.getProfiler().getPath().toString().endsWith(".json") ||
                parameters.getProfiler().getPath().toString().endsWith(".json.gz")) {
            return new FileProfiler(parameters.getProfiler().getPath()).profile();
        }
        final Profile profile = new WordReaderProfiler(
                wordReader,
                new LocalProfilerProcess(
                        parameters.getProfiler().getPath(),
                        parameters.getProfiler().getConfig(),
                        alex)
        ).profile();
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cachedPath.toFile())), StandardCharsets.UTF_8))) {
            w.write(profile.toJSON());
            w.write('\n');
        }
        return profile;
    }
}
