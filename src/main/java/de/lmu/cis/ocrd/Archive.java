package de.lmu.cis.ocrd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

public class Archive {

	public static boolean isABBYYLine(String name) {
		return name.endsWith(".xml") && name.contains("abbyy");
	}

	public static boolean isOcropusLine(String name) {
		return name.endsWith(".txt") && !name.endsWith(".gt.txt");
	}

	public static String slurpZipFile(ZipFile zip, String path) throws IOException {
		ZipEntry entry = zip.getEntry(path);
		try (InputStream in = zip.getInputStream(entry)) {
			return IOUtils.toString(in, Charset.forName("UTF-8"));
		}
	}
}
