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

public class Profiler {
    private String exe, language, workdir, langdir;
    private String[] args;
    private Reader stdin;

    public Profiler withStdin(Reader r) {
        this.stdin = r;
        return this;
    }

    public Profiler withExecutable(String exe) {
        this.exe = exe;
        return this;
    }

    public Profiler withWorkDirectory(String workdir) {
        this.workdir = workdir;
        return this;
    }
    public Profiler withLanguage(String language) {
        this.language = language;
        return this;
    }

    public Profiler withLanguageDirectory(String langdir) {
        this.langdir = langdir;
        return this;
    }

    public Profiler withArgs(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public String toString() {
        return String.join(" ", makeArgs());
    }

    public Profile run() throws IOException {
        Process profiler = createCommand();
        // write stdin to profiler
        IOUtils.copy(stdin, profiler.getOutputStream(), Charset.defaultCharset());
        // read profile from profiler's stdout
        return Profile.read(profiler.getInputStream());
    }

    private Process createCommand() throws IOException {
       ProcessBuilder builder = new ProcessBuilder();
       builder.command(makeArgs());
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
