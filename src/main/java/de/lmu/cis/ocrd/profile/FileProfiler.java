package de.lmu.cis.ocrd.profile;

import java.io.Reader;

// Simple Profiler that reads a profiler from a given json profile file.
public class FileProfiler implements Profiler {
	@Override
	public Profile profile(Reader r) throws Exception {
		return Profile.read(r);
	}
}
