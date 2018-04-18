package de.lmu.cis.ocrd.parsers;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.Entry;

public class ArchiveParser implements Parser {

	private final static Pattern num = Pattern.compile(".*?(\\p{Digit}+)\\..*?");

	private static int getPageID(Path path) throws Exception {
		Matcher m = num.matcher(path.getFileName().toString());
		if (!m.matches()) {
			throw new Exception("cannot extract pageid from file name: " + path.toString());
		}
		return Integer.parseInt(m.group(1));
	}

	private final XMLParserFactory factory;
	private final XMLFileType fileType;

	private final Archive archive;

	public ArchiveParser(XMLParserFactory f, XMLFileType t, Archive archive) {
		this.factory = f;
		this.fileType = t;
		this.archive = archive;
	}

	private final ArrayList<Entry> gatherPages() {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for (Entry entry : this.archive) {
			if (!this.fileType.check(entry.getName().toString())) {
				continue;
			}
			entries.add(entry);
		}
		return entries;
	}

	@Override
	public SimpleDocument parse() throws Exception {
		ArrayList<Entry> pages = gatherPages();
		SimpleDocument doc = new SimpleDocument().withPath(this.archive.getName().toString());
		for (Entry entry : pages) {
			doc.add(parsePage(entry, getPageID(entry.getName())));
		}
		return doc;
	}

	private final SimpleDocument parsePage(Entry entry, int pageID) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		// make the parsing faster
		docBuilderFactory.setValidating(false); // do *not* validate DTDs; its too slow.
		docBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
		docBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
		docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputStream is = this.archive.open(entry);
		org.w3c.dom.Document xml = docBuilder.parse(is);
		return this.factory.create(xml, pageID).parse();
	}

}
