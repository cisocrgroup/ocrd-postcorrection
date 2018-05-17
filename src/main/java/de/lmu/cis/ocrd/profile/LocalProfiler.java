package de.lmu.cis.ocrd.profile;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalProfiler implements Profiler {
    private String exe, language, workdir, langdir;
    private String[] args;
    private Reader stdin;

    public LocalProfiler() {
        this.exe = "profiler";
        this.workdir = ".";
        this.langdir = "/data";
        this.args = new String[]{"--sourceFormat", "TXT"};
    }

    public LocalProfiler withStdin(Reader r) {
        this.stdin = r;
        return this;
    }

    public LocalProfiler withExecutable(String exe) {
        this.exe = exe;
        return this;
    }

    public LocalProfiler withWorkDirectory(String workdir) {
        this.workdir = workdir;
        return this;
    }
    public LocalProfiler withLanguage(String language) {
        this.language = language;
        return this;
    }

    public LocalProfiler withLanguageDirectory(String langdir) {
        this.langdir = langdir;
        return this;
    }

    public LocalProfiler withArgs(String... args) {
        this.args = args;
        return this;
    }

    @Override
    public String toString() {
        return String.join(" ", makeArgs());
    }

    @Override
    public Profile profile() throws Exception {
        Process profiler = createCommand();
        // write stdin to profiler
        IOUtils.copy(stdin, profiler.getOutputStream(), Charset.defaultCharset());
        profiler.getOutputStream().flush();
        profiler.getOutputStream().close();
        // read profile from profiler's stdout
        Profile profile =  Profile.read(profiler.getInputStream());
        final int exitStatus = profiler.waitFor();
        if (exitStatus != 0) {
            throw new Exception("profiler returned with exit value: " + exitStatus);
        }
        return profile;
    }

    private Process createCommand() throws IOException {
       ProcessBuilder builder = new ProcessBuilder();
       builder.command(makeArgs());
       // TODO: check if the work-dir really is needed
       builder.directory(new File(this.workdir));
       return builder.start();
    }

    private List<String> makeArgs() {
        List<String> res = new ArrayList<>();
        res.add(exe);
        res.addAll(Arrays.asList(defaultArgs()));
        res.add("--config");
        res.add(Paths.get(langdir, language + ".ini").toAbsolutePath().toString());
        res.addAll(Arrays.asList(this.args));
        return res;
    }

    private static String[] defaultArgs() {
        return new String[]{
                "--sourceFile",
                "/dev/stdin",
                "--jsonOutput"
        };
    }
}
