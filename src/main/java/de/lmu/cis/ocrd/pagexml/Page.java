package de.lmu.cis.ocrd.pagexml;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Page {
	// Open a page from a page-XML file path.
	public static Page open(Path path) throws Exception {
		File file = path.toFile();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		return new Page(builder.parse(file));
	}

	private final List<Line> lines;

	public Page(Document doc) throws XPathExpressionException {
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
