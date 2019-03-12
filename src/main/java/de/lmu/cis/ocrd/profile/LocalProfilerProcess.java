package de.lmu.cis.ocrd.profile;

import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalProfilerProcess implements ProfilerProcess {

	private final Path executable;
	private final Path config;
	private final AdditionalLexicon additionalLex;
	private String[] args;

	public LocalProfilerProcess(Path executable, Path config, AdditionalLexicon additionalLex) {
		this.executable = executable;
		this.config = config;
		this.additionalLex = additionalLex;
	}

	public LocalProfilerProcess withArgs(String... args) {
		this.args = args;
		return this;
	}

	@Override
	public String toString() {
		return String.join(" ", makeArgs());
	}

	@Override
	public void profile(InputStream is, OutputStream out) throws Exception {
		final Charset utf8 = Charset.forName("UTF-8");
		Process profiler = startProfiler();
		try (BufferedReader stderr = new BufferedReader(new InputStreamReader(profiler.getErrorStream(), utf8));
			 Writer stdin = new BufferedWriter(new OutputStreamWriter(profiler.getOutputStream(), utf8));
			 Reader stdout = new BufferedReader(new InputStreamReader(profiler.getInputStream(), utf8))) {

			Thread t1 = new Thread(logStderr(stderr));
			Thread t2 = new Thread(writeStdin(is, stdin));
			Thread t3 = new Thread(readStdout(stdout, out));
			t1.start();
			t2.start();
			t3.start();
			t1.join();
			t2.join();
			t3.join();
			final int exitStatus = profiler.waitFor();
			if (exitStatus != 0) {
				throw new Exception("profiler returned with exit status: " + exitStatus);
			}
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

	private Runnable writeStdin(InputStream is, Writer stdin) {
		return () -> {
			try {
				if (additionalLex.use()) {
					for (String entry: additionalLex.entries()) {
						stdin.write('#');
						stdin.write(entry);
						stdin.write('\n');
					}
				}
				IOUtils.copy(is, stdin, Charset.forName("UTF-8"));
				stdin.flush();
				stdin.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	private Runnable readStdout(Reader stdout, OutputStream os) {
		return () -> {
			try {
				IOUtils.copy(stdout, os, Charset.forName("UTF-8"));
				os.flush();
				os.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private Process startProfiler() throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		final List<String> command = makeArgs();
		builder.command(command);
		Logger.info("profiler command: " + String.join(" ", command));
		return builder.start();
	}

	private List<String> makeArgs() {
		List<String> res = new ArrayList<>();
		res.add(executable.toString());
		res.add("--sourceFormat");
		res.add("EXT");
		res.add("--config");
		res.add(config.toString());
		res.add("--sourceFile");
		res.add("/dev/stdin");
		res.add("--jsonOutput");
		res.add("/dev/stdout");
		res.add("--types");
		if (this.args != null) {
			res.addAll(Arrays.asList(this.args));
		}
		return res;
	}
}
