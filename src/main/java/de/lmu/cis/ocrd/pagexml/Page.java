package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Page {
	public static final String MIMEType = "application/vnd.prima.page+xml";
	private final Document doc;

	// Open a page from a page-XML file path.
	public static Page open(Path path) throws Exception {
		try (final InputStream is = new FileInputStream(path.toFile())) {
			return parse(is);
		}
	}

	public static Page parse(InputStream is) throws Exception {
		final DocumentBuilder builder = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder();
		return new Page(builder.parse(is));
	}

	private final List<Line> lines;

	public Page(Document doc) throws XPathExpressionException {
		this.doc = doc;
		this.lines = getLineNodes(doc);
	}

	public List<Line> getLines() {
		return this.lines;
	}

	private static List<Line> getLineNodes(Document doc)
			throws XPathExpressionException {
		ArrayList<Line> nodeList = new ArrayList<>();
		for (Node node : XPathHelper.getNodes(doc,
				"/PcGts/Page/TextRegion/TextLine")) {
			nodeList.add(new Line(node));
		}
		return nodeList;
	}
}
