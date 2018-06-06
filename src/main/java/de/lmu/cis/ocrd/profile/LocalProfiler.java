package de.lmu.cis.ocrd.profile;

import de.lmu.cis.ocrd.Document;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalProfiler implements Profiler {
    private String exe, language, workdir, langdir;
    private String[] args;
	private Document inputDocument;
	private Path outputPath;
	private Path inputPath;

	public LocalProfiler() {
        this.exe = "profiler";
        this.workdir = ".";
        this.langdir = "/data";
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

	public LocalProfiler withInputPath(Path path) {
		this.inputPath = path;
		return this;
	}

	public LocalProfiler withOutputPath(Path path) {
		this.outputPath = path;
		return this;
	}

    @Override
    public String toString() {
        return String.join(" ", makeArgs());
    }

    @Override
    public Profile profile() throws Exception {
		Process profiler = startCommand();
        final int exitStatus = profiler.waitFor();
        if (exitStatus != 0) {
            throw new Exception("profiler returned with exit value: " + exitStatus);
        }
		return Profile.read(outputPath);
    }

	private Process startCommand() throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		final List<String> command = makeArgs();
		builder.command(command);
		// TODO: check if the work-dir really is needed
		builder.directory(new File(this.workdir));
		Logger.info("profiler command: " + String.join(" ", command));
		return builder.start();
    }

	private List<String> makeArgs() {
        List<String> res = new ArrayList<>();
        res.add(exe);
        res.addAll(Arrays.asList(defaultArgs()));
        res.add("--config");
        res.add(Paths.get(langdir, language + ".ini").toAbsolutePath().toString());
		res.add("--sourceFile");
		res.add(inputPath.toString());
		res.add("--jsonOutput");
		res.add(outputPath.toString());
		if (this.args != null) {
			res.addAll(Arrays.asList(this.args));
		}
        return res;
    }

    private static String[] defaultArgs() {
        return new String[]{
                "--sourceFile",
                "/dev/stdin",
				"--sourceFormat",
				"TXT",
        };
    }
}
