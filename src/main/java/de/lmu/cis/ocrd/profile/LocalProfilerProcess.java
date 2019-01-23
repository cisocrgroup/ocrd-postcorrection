package de.lmu.cis.ocrd.profile;

import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalProfilerProcess implements ProfilerProcess {

	private final String executable;
	private final Path config;
	private String[] args;

	public LocalProfilerProcess(String executable, Path config) {
		this.executable = executable;
		this.config = config;
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
	public Reader profile(InputStream is) throws Exception {
		final Charset utf8 = Charset.forName("UTF-8");
		Process profiler = startCommand();
		Reader stdout = null;
		try (BufferedReader stderr = new BufferedReader(new InputStreamReader(profiler.getErrorStream(), utf8));
			 Writer stdin = new BufferedWriter(new OutputStreamWriter(profiler.getOutputStream(), utf8))) {
			stdout = new BufferedReader(new InputStreamReader(profiler.getInputStream(), utf8));
			Thread t1 = new Thread(logStderr(stderr));
			Thread t2 = new Thread(writeStdin(is, stdin));
			t1.start();
			t2.start();
			final int exitStatus = profiler.waitFor();
			if (exitStatus != 0) {
				throw new Exception("profiler returned with exit status: " + exitStatus);
			}
			t1.join();
			t2.join();
		} catch (Exception e) {
			if (stdout != null) {
				stdout.close();
			}
		}
		return stdout;
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
				IOUtils.copy(is, stdin, Charset.forName("UTF-8"));
				stdin.flush();
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
		res.add(executable);
		res.add("--sourceFormat");
		res.add("TXT");
		res.add("--config");
		res.add(config.toString());
		res.add("--sourceFile");
		res.add("/dev/stdin");
		res.add("--jsonOutput");
		res.add("/dev/stdout");
		if (this.args != null) {
			res.addAll(Arrays.asList(this.args));
		}
		return res;
	}
}
