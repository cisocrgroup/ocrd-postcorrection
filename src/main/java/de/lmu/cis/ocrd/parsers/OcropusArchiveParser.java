package de.lmu.cis.ocrd.parsers;

import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.Entry;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OcropusArchiveParser implements Parser {
    protected Line readLine(InputStream is, int pageID, int lineID) throws Exception {
        final String ocr = IOUtils.toString(is, Charset.forName("UTF-8"));
		return SimpleLine.normalized(ocr, 0).withPageID(pageID).withLineID(lineID);
	}

	private static void sort(List<Entry> lines) {
		lines.sort((a, b) -> {
			int c = a.getName().getParent().getFileName().toString()
					.compareTo(b.getName().getParent().getFileName().toString());
			if (c != 0) {
				return c;
			}
			return a.getName().toString().compareTo(b.getName().toString());
		});
	}

	private final OCRFileType ocrFileType;
	private final Archive ar;

	public OcropusArchiveParser(Archive ar) {
	    this(ar, new OcropusFileType());
	}

	OcropusArchiveParser(Archive ar, OCRFileType filetype) {
	    this.ocrFileType = filetype;
	    this.ar = ar;
    }

	@Override
	public SimpleDocument parse() throws Exception {
		// gather valid `.txt` files
		ArrayList<Entry> entries = new ArrayList<>();
		for (Entry entry : this.ar) {
			if (!ocrFileType.check(entry.getName())) {
				continue;
			}
			entries.add(entry);
		}
		sort(entries);
		// append lines to document
		SimpleDocument doc = new SimpleDocument().withPath(ar.getName().toString());
		HashMap<Integer, Integer> lineIDs = new HashMap<>();
		for (Entry entry : entries) {
			int pageno = Integer.parseInt(entry.getName().getParent().getFileName().toString());
			if (!lineIDs.containsKey(pageno)) {
				lineIDs.put(pageno, 0);
			}
			final int lid = lineIDs.get(pageno) + 1;
			try (InputStream is = ar.open(entry)) {
			    doc.add(pageno, readLine(is, pageno, lid));
            }
			lineIDs.put(pageno, lid);
		}
		ar.close();
		return doc;
	}
}
