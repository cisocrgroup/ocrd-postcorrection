package de.lmu.cis.ocrd.profile;

import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalProfiler implements Profiler {
	private String exe, language, langdir;
	private String[] args;

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
		return String.join(" ", makeArgs());
	}

	@Override
	public Profile profile(Reader r) throws Exception {
		Process profiler = startCommand();
		try (// InputStream stdout = profiler.getInputStream();
		     // we do not care about stderr's encoding
		     BufferedReader stderr = new BufferedReader(
				new InputStreamReader(profiler.getErrorStream()));
		     Writer stdin = new BufferedWriter(
					new OutputStreamWriter(profiler.getOutputStream(),
							Charset.forName("UTF-8")))) {
			Thread t1 = new Thread(logStderr(stderr));
			Thread t2 = new Thread(writeStdin(r, stdin));
			t1.start();
			t2.start();
			//final Profile profile = Profile.read(stdout);
			final int exitStatus = profiler.waitFor();
			if (exitStatus != 0) {
				throw new Exception(
						"profiler returned with exit value: " + exitStatus);
			}
			t1.join();
			t2.join();
//			if (profile == null) {
//				throw new Exception("profiler did not return a valid profile");
//			}
			return null;
		}
	}

	private Runnable logStderr(BufferedReader stderr) {
		return () -> {
			try {
				String line;
				while ((line = stderr.readLine()) != null) {
					Logger.debug(line);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private Runnable writeStdin(Reader in, Writer stdin) {
		return () -> {
			try {
				String input = IOUtils.toString(in);
				IOUtils.write(input, stdin);
				stdin.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private Process startCommand() throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		final List<String> command = makeArgs();
		builder.command(command);
		Logger.info("profiler command: " + String.join(" ", command));
		return builder.start();
	}

	private List<String> makeArgs() {
		List<String> res = new ArrayList<>();
		res.add(exe);
		res.addAll(Arrays.asList(defaultArgs()));
		res.add("--config");
		res.add(Paths.get(langdir, language + ".ini").toAbsolutePath()
				.toString());
		res.add("--sourceFile");
		res.add("/dev/stdin");
		res.add("--jsonOutput");
		res.add("/tmp/profile.json");
		// res.add("/dev/stdout");
		if (this.args != null) {
			res.addAll(Arrays.asList(this.args));
		}
		return res;
	}
}
