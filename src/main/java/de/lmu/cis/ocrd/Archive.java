package de.lmu.cis.ocrd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

public class Archive {
	public static SimpleDocument createOcropusDocumentFromZipArchive(String ar) throws IOException {
		try (ZipFile zip = new ZipFile(ar)) {
			return createOcropusDocumentFromZipArchive(zip);
		}
	}

	public static SimpleDocument createOcropusDocumentFromZipArchive(ZipFile zip) throws IOException {
		// gather valid `.txt` files
		ArrayList<Path> lines = new ArrayList<Path>();
		for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
			ZipEntry entry = entries.nextElement();
			if (!isOcropusLine(entry.getName())) {
				continue;
			}
			Path path = Paths.get(entry.getName());
			if (path.getParent() == null) {
				continue;
			}
			lines.add(path);
		}
		// sort by directory and then by filename
		Collections.sort(lines, new Comparator<Path>() {
			@Override
			public int compare(Path a, Path b) {
				int c = a.getParent().getFileName().toString().compareTo(b.getParent().getFileName().toString());
				if (c != 0) {
					return c;
				}
				return a.getFileName().compareTo(b.getFileName());
			}
		});
		// append lines to document
		SimpleDocument doc = new SimpleDocument().withPath(zip.getName()).withOcrEngine("ocropus");
		for (Path line : lines) {
			int pageno = Integer.parseInt(line.getParent().getFileName().toString());
			doc.add(pageno, slurpZipFile(zip, line.toString()));
		}
		return doc;
	}

	private static boolean isOcropusLine(String name) {
		return name.endsWith(".txt") && !name.endsWith(".gt.txt");
	}

	private static String slurpZipFile(ZipFile zip, String path) throws IOException {
		ZipEntry entry = zip.getEntry(path);
		try (InputStream in = zip.getInputStream(entry)) {
			return IOUtils.toString(in, Charset.forName("UTF-8"));
		}
	}
}
