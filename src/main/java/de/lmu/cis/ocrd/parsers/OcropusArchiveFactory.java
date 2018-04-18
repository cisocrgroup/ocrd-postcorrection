package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.SimpleLine;

public class OcropusArchiveFactory extends ArchiveFactory {
	public OcropusArchiveFactory(String ar) {
		super(ar);
	}

	@Override
	public SimpleDocument create(ZipFile zip) throws Exception {
		// gather valid `.txt` files
		ArrayList<Path> lines = new ArrayList<Path>();
		for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
			ZipEntry entry = entries.nextElement();
			if (!Archive.isOcropusLine(entry.getName())) {
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
		HashMap<Integer, Integer> lineIDs = new HashMap<Integer, Integer>();
		for (Path line : lines) {
			int pageno = Integer.parseInt(line.getParent().getFileName().toString());
			if (!lineIDs.containsKey(pageno)) {
				lineIDs.put(pageno, 0);
			}
			final int lid = lineIDs.get(pageno) + 1;
			String ocr = Archive.slurpZipFile(zip, line.toString());
			doc.add(pageno, new SimpleLine().withOcr(ocr).withPageId(pageno).withLineId(lid));
			lineIDs.put(pageno, lid);
		}
		return doc;
	}
}
