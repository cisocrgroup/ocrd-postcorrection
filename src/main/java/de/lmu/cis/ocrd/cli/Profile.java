package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profiler;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.writers.ConsoleWriter;

import java.io.*;
import java.util.Optional;

public class Profile implements Closeable {
	private final Profiler profiler;
	private final File inputFile, outputFile;

	private Profile(LocalProfiler profiler) throws IOException {
		this.inputFile = File.createTempFile("cis-profiler-in", ".txt");
		this.inputFile.deleteOnExit();
		this.outputFile = File.createTempFile("cis-profiler-out", ".json");
		this.outputFile.deleteOnExit();
		this.profiler = profiler;
	}

	public static void main(String[] args) {
		Configurator.
				currentConfig().
				writer(new ConsoleWriter(System.err)).
				activate();
		final Optional<LocalProfiler> profiler = parseArgs(args);
		if (!profiler.isPresent()) {
			throw new RuntimeException("invalid or missing arguments; expected: exe dir language");
		}
		try (final Profile profile = new Profile(profiler.get())) {
			profile.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Optional<LocalProfiler> parseArgs(String[] args) {
		if (args.length != 3) {
			return Optional.empty();
		}
		return Optional.of(new LocalProfiler().
				withExecutable(args[0]).
				withLanguageDirectory(args[1]).
				withLanguage(args[2]).
				withArgs("--types")
		);
	}

	private static void writeProfileToStdout(de.lmu.cis.ocrd.profile.Profile profile) {
		System.out.println(new Gson().toJson(profile));
	}

	@Override
	public void close() {
		inputFile.delete();
		outputFile.delete();
	}

	private void run() throws Exception {
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
