package de.lmu.cis.ocrd.profile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;

// Simple Profiler that reads a profiler from a given json profile file.
public class FileProfiler implements Profiler {
	private Path path;

	public FileProfiler(Path path) {
		this.path = path;
	}

	@Override
	public Profile profile() throws Exception {
		final Charset utf8 = Charset.forName("UTF-8");
		try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()), utf8))) {
			return Profile.read(r);
		}
	}
}
