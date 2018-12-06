package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profiler;

import java.io.*;

public class Profile implements Closeable {
	private final Profiler profiler;
	private final File inputFile, outputFile;

	public Profile(LocalProfiler profiler) throws IOException {
		this.inputFile = File.createTempFile("cis-profiler-in", ".txt");
		this.inputFile.deleteOnExit();
		this.outputFile = File.createTempFile("cis-profiler-out", ".json");
		this.outputFile.deleteOnExit();
		this.profiler = profiler;
	}

	private static void writeProfileToStdout(de.lmu.cis.ocrd.profile.Profile profile) {
		System.out.println(new Gson().toJson(profile));
	}

	@Override
	public void close() {
		inputFile.delete();
		outputFile.delete();
	}

	public void run() throws Exception {
		writeStdinToFile();
		final de.lmu.cis.ocrd.profile.Profile profile = profiler.profile(inputFile.toPath());
		writeProfileToStdout(profile);
	}

	private void writeStdinToFile() throws IOException {
		try (BufferedWriter out = new BufferedWriter(
				new FileWriter(inputFile));
		     BufferedReader in = new BufferedReader(
				     new InputStreamReader(System.in))
		) {
			String line;
			while ((line = in.readLine()) != null) {
				out.write(line);
				out.newLine();
			}
		}
	}
}
