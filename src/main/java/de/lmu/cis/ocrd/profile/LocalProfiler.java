package de.lmu.cis.ocrd.profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pmw.tinylog.Logger;

public class LocalProfiler implements Profiler {
	private String exe, language, langdir;
	private String[] args;
	private Path outputPath;
	private Path inputPath;

	public LocalProfiler() {
		this.exe = "profiler";
		this.langdir = "/data";
	}

	private static String[] defaultArgs() {
		return new String[] { "--sourceFile", "/dev/stdin", "--sourceFormat",
				"TXT", };
	}

	public LocalProfiler withExecutable(String exe) {
		this.exe = exe;
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
		return String.join(" ", makeArgs(Paths.get("/path/to/input-file")));
	}

	@Override
	public Profile profile(Path path) throws Exception {
		Process profiler = startCommand(path);
		try (Reader r = new BufferedReader(
				new InputStreamReader(profiler.getInputStream()))) {
			Profile profile = Profile.read(r);
			final int exitStatus = profiler.waitFor();
			if (exitStatus != 0) {
				throw new Exception(
						"profiler returned with exit value: " + exitStatus);
			}
			return profile;
		}
	}

	private Process startCommand(Path path) throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		final List<String> command = makeArgs(path);
		builder.command(command);
		Logger.info("profiler command: " + String.join(" ", command));
		return builder.start();
	}

	private List<String> makeArgs(Path path) {
		List<String> res = new ArrayList<>();
		res.add(exe);
		res.addAll(Arrays.asList(defaultArgs()));
		res.add("--config");
		res.add(Paths.get(langdir, language + ".ini").toAbsolutePath()
				.toString());
		res.add("--sourceFile");
		res.add(path.toString());
		res.add("--jsonOutput");
		res.add("/dev/stdout");
		if (this.args != null) {
			res.addAll(Arrays.asList(this.args));
		}
		return res;
	}
}
