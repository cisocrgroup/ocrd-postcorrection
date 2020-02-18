package de.lmu.cis.ocrd.profile.test;

import de.lmu.cis.ocrd.profile.AdditionalFileLexicon;
import de.lmu.cis.ocrd.profile.LocalProfilerProcess;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.ProfilerProcess;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LocalProfilerProcessTest {
    private static final Path exe = Paths.get("profiler");
    private static final Path config = Paths.get("config.ini");

    private List<String> getDefault() {
        return Arrays.asList(
                exe.toString(),
                "--sourceFormat", "EXT",
                "--config",
                config.toString(),
                "--sourceFile",
                "/dev/stdin",
                "--jsonOutput",
                "/dev/stdout",
                "--types"
        );
    }

    @Test
    public void testWithEmptyAdditionalLexicon() {
        final ProfilerProcess p = new LocalProfilerProcess(exe, config, new NoAdditionalLexicon());
        final String want = String.join(" ", getDefault());
        assertThat(p.toString(), is(want));
    }

    @Test
    public void testWithAdditionalLexicon() {
        final ProfilerProcess p = new LocalProfilerProcess(exe, config, new AdditionalFileLexicon(Paths.get("addLex")));
        final List<String> def = new ArrayList<>();
        def.addAll(getDefault());
        final String want = String.join(" ", def);
        assertThat(p.toString(), is(want));
    }
}
