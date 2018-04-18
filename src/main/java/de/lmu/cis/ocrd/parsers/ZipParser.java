package de.lmu.cis.ocrd.parsers;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.lmu.cis.ocrd.SimpleDocument;

public class ZipParser implements Parser {

	private final static Pattern num = Pattern.compile(".*?(\\p{Digit}+).*?");

	private static int getPageID(Path path) throws Exception {
		Matcher m = num.matcher(path.getFileName().toString());
		if (!m.matches()) {
			throw new Exception("cannot extract pageid from file name: " + path.toString());
		}
		return Integer.parseInt(m.group(1));
	}

	private final XMLParserFactory factory;
	private final XMLFileType fileType;
	private final ZipFile zip;

	public ZipParser(XMLParserFactory f, XMLFileType t, ZipFile zip) {
		this.factory = f;
		this.fileType = t;
		this.zip = zip;
	}

	private final ArrayList<Path> gatherPages() {
		ArrayList<Path> pages = new ArrayList<Path>();
		for (Enumeration<? extends ZipEntry> entries = this.zip.entries(); entries.hasMoreElements();) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory() || !this.fileType.check(entry.getName())) {
				continue;
			}
			pages.add(Paths.get(entry.getName()));
		}
		return pages;
	}

	@Override
	public SimpleDocument parse() throws Exception {
		ArrayList<Path> pages = gatherPages();
		SimpleDocument doc = new SimpleDocument().withPath(this.zip.getName());
		for (Path page : pages) {
			doc.add(parsePage(page, getPageID(page)));
		}
		return doc;
	}

	private final SimpleDocument parsePage(Path page, int pageID) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputStream is = this.zip.getInputStream(zip.getEntry(page.toString()));
		org.w3c.dom.Document xml = docBuilder.parse(is);
		return this.factory.create(xml, pageID).parse();
	}

}
