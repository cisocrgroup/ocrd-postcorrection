package de.lmu.cis.ocrd.profile;

import java.io.Reader;

// Simple Profiler that reads a profiler from a given json profile file.
public class FileProfiler implements Profiler {
	private Reader reader;

	@Override
	public void profile(Reader r) throws Exception {
		this.reader = r;
	}

	@Override
	public Profile getProfile() throws Exception {
		return Profile.read(reader);
	}
}
