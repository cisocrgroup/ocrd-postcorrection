package de.lmu.cis.ocrd;

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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ABBYYDocumentBuilder extends ArchiveFactory {
	private final static Pattern num = Pattern.compile(".*?(\\p{Digit}+).*?");

	private static int getPageID(Path path) throws Exception {
		Matcher m = num.matcher(path.getFileName().toString());
		if (!m.matches()) {
			throw new Exception("cannot extract pageid from file name: " + path.getFileName().toString());
		}
		return Integer.parseInt(m.group(1));
	}

	private static String parseChar(Node charParam) throws Exception {
		Node data = charParam.getFirstChild();
		if (data == null) {
			return " ";
		}
		if (data.getNodeType() != Node.TEXT_NODE) {
			throw new Exception("invalid charParams node");
		}
		String r = data.getNodeValue();
		if (r == null || "".equals(r)) {
			return " ";
		}
		return r;
	}

	private static double parseConfidence(Node charParam) {
		if (charParam.getAttributes() == null) {
			return 0;
		}
		Node c = charParam.getAttributes().getNamedItem("charConfidence");
		if (c == null) {
			return 0;
		}
		return (double) 1 / (double) Integer.parseInt(c.getNodeValue());
	}

	private SimpleDocument doc;

	public ABBYYDocumentBuilder(String ar) {
		super(ar);
	}

	@Override
	protected SimpleDocument create(ZipFile zip) throws Exception {
		// gather XML ABBYY files
		ArrayList<Path> pages = new ArrayList<Path>();
		for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
			ZipEntry entry = entries.nextElement();
			if (!Archive.isABBYYLine(entry.getName())) {
				continue;
			}
			pages.add(Paths.get(entry.getName()));
		}
		// parse XML files
		this.doc = new SimpleDocument().withPath(zip.getName());
		for (Path path : pages) {
			parsePage(zip, path);
		}
		return doc;
	}

	private void parseLine(int pid, int lid, Node line) throws Exception {
		if (line.getNodeType() != Node.ELEMENT_NODE) {
			return;
		}
		NodeList charParams = ((Element) line).getElementsByTagName("charParams");
		final int n = charParams.getLength();
		StringBuilder str = new StringBuilder();
		ArrayList<Double> cs = new ArrayList<Double>();
		for (int i = 0; i < n; i++) {
			String r = parseChar(charParams.item(i));
			double c = parseConfidence(charParams.item(i));
			str.append(r);
			cs.add(c);
		}
		SimpleLine tmp = new SimpleLine().withOcr(str.toString()).withConfidences(cs).withLineId(lid).withPageId(pid);
		this.doc.add(pid, tmp);
	}

	private void parsePage(int pid, InputStream is) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document xml = docBuilder.parse(is);
		parsePage(pid, xml);
	}

	private void parsePage(int pid, org.w3c.dom.Document xml) throws Exception {
		NodeList lines = xml.getElementsByTagName("line");
		final int n = lines.getLength();
		for (int i = 0; i < n; i++) {
			parseLine(pid, i + 1, lines.item(i));
		}
	}

	private void parsePage(ZipFile zip, Path path) throws Exception {
		final int pid = getPageID(path);
		try (InputStream is = zip.getInputStream(zip.getEntry(path.toString()))) {
			parsePage(pid, is);
		}
	}
}
