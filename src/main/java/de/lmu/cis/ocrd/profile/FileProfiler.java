package de.lmu.cis.ocrd.profile;

import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

// Simple Profiler that reads a profiler from a given json profile file.
public class FileProfiler implements Profiler {
	private Path path;

	public FileProfiler(Path path) {
		this.path = path;
	}

	@Override
	public Profile profile() throws Exception {
		try (Reader r = open()) {
			return Profile.read(r);
		}
	}

	private Reader open() throws Exception {
		final Charset utf8 = Charset.forName("UTF-8");
		final String mime = Files.probeContentType(path);
		switch (mime) {
			case "application/json":
				return new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()), utf8));
			case "application/gzip":
			case "application/x-gzip":
				return new BufferedReader(
						new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile())), utf8));
			default:
				throw new Exception("Unsupported file format for profile: " + mime);
		}
	}
}
