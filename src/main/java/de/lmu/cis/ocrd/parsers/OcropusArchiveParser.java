package de.lmu.cis.ocrd.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;

import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.Entry;

public class OcropusArchiveParser implements Parser {
	private static boolean isOcropusLine(String name) {
		return name.endsWith(".txt") && !name.endsWith(".gt.txt");
	}

	private static String slurpArchiveFile(Archive ar, Entry entry) throws IOException {
		try (InputStream in = ar.open(entry)) {
			return IOUtils.toString(in, Charset.forName("UTF-8"));
		}
	}

	private static void sort(List<Entry> lines) {
		Collections.sort(lines, new Comparator<Entry>() {
			@Override
			public int compare(Entry a, Entry b) {
				int c = a.getName().getParent().getFileName().toString()
						.compareTo(b.getName().getParent().getFileName().toString());
				if (c != 0) {
					return c;
				}
				return a.getName().toString().compareTo(b.getName().toString());

			}
		});
	}

	private final Archive ar;

	public OcropusArchiveParser(Archive ar) {
		this.ar = ar;
	}

	@Override
	public SimpleDocument parse() throws Exception {
		// gather valid `.txt` files
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for (Entry entry : this.ar) {
			if (!isOcropusLine(entry.getName().toString())) {
				continue;
			}
			entries.add(entry);
		}
		sort(entries);
		// append lines to document
		SimpleDocument doc = new SimpleDocument().withPath(ar.getName().toString());
		HashMap<Integer, Integer> lineIDs = new HashMap<Integer, Integer>();
		for (Entry entry : entries) {
			int pageno = Integer.parseInt(entry.getName().getParent().getFileName().toString());
			if (!lineIDs.containsKey(pageno)) {
				lineIDs.put(pageno, 0);
			}
			final int lid = lineIDs.get(pageno) + 1;
			String ocr = slurpArchiveFile(ar, entry);
			doc.add(pageno, new SimpleLine().withOcr(ocr).withPageId(pageno).withLineId(lid));
			lineIDs.put(pageno, lid);
		}
		return doc;
	}
}
