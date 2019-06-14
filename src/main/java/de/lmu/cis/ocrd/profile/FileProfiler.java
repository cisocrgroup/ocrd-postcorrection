package de.lmu.cis.ocrd.profile;

import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.URLConnection;
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
		final String mime = getMIMEType(path);
		Logger.debug("mime type for {}: {}", path.toString(), mime);
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

	private static String getMIMEType(Path path) throws Exception {
		// try probeContentType
		String mime = Files.probeContentType(path);
		switch (mime) {
			case "application/json":
			case "application/gzip":
			case "application/x-gzip":
				return mime;
		}

		// try guessContentType
        try (InputStream is = new BufferedInputStream(new FileInputStream(path.toFile()))) {
			mime = URLConnection.guessContentTypeFromStream(is);
			if (mime != null) {
				return mime;
			}
        }
        // use file extension
		int pos = path.toString().lastIndexOf('.');
		if (pos > 0) {
			switch (path.toString().substring(pos)) {
				case ".json":
					return "application/json";
				case ".gz":
					return "application/x-gzip";
			}
		}
		// unknown mime type
		return "application/octet-stream";
    }
}
