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
	private String exe, language, languageDir;
	private String[] args;
	private final File profilerInputFile, profilerOutputFile;

	public LocalProfiler() throws Exception {
		this.exe = "profiler";
		this.languageDir = "/data";
		profilerInputFile = File.createTempFile("ocrd-cis-profiler-input-", ".txt");
		profilerOutputFile = File.createTempFile("ocrd-cis-proilfer-output-", ".json");
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
		this.languageDir = langdir;
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
	public void profile(Reader r) throws Exception {
		writeProfilerInputFile(r);
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
			final int exitStatus = profiler.waitFor();
			if (exitStatus != 0) {
				throw new Exception(
						"profiler returned with exit value: " + exitStatus);
			}
			t1.join();
			t2.join();
		}
	}

	@Override
	public Profile getProfile() throws Exception {
		return readProfilerOutputFile();
	}

	private void writeProfilerInputFile(Reader r) throws Exception {
		final Charset utf8 = Charset.forName("UTF-8");
		try (OutputStream os = new FileOutputStream(profilerInputFile)) {
			IOUtils.copy(r, os, utf8);
		}
	}

	private Profile readProfilerOutputFile() throws Exception {
		final Charset utf8 = Charset.forName("UTF-8");
		try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(profilerOutputFile), utf8))) {
			return Profile.read(reader);
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
		res.add(Paths.get(languageDir, language + ".ini").toAbsolutePath()
				.toString());
		res.add("--sourceFile");
		res.add(profilerInputFile.toString());
		res.add("--jsonOutput");
		res.add(profilerOutputFile.toString());
		// res.add("/dev/stdout");
		if (this.args != null) {
			res.addAll(Arrays.asList(this.args));
		}
		return res;
	}
}
