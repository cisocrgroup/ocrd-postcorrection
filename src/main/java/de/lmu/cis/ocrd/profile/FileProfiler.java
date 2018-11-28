package de.lmu.cis.ocrd.profile;

import java.io.IOException;
import java.nio.file.Path;

// Simple Profiler that reads a profiler from a given json profile file.
public class FileProfiler implements Profiler {
	@Override
	public Profile profile(Path path) throws IOException {
		return Profile.read(path);
	}
}
