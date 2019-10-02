package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Page {
	public static final String MIMEType = "application/vnd.prima.page+xml";
	private final Document doc;
	private final Path path;

	// Open a page from a page-XML file path.
	public static Page open(Path path) throws Exception {
		try (final InputStream is = new FileInputStream(path.toFile())) {
			return parse(path, is);
		}
	}

	public static Page parse(Path path, InputStream is) throws Exception {
		final DocumentBuilder builder = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder();
		return new Page(path, builder.parse(is));
	}

	private final List<Line> lines;

	public Page(Path path, Document doc) {
		this.path = path;
		this.doc = doc;
		this.lines = getLineNodes(doc);
	}

	public Path getPath() {
		return path;
	}

	public List<Line> getLines() {
		return this.lines;
	}

	private List<Line> getLineNodes(Document doc) {
		ArrayList<Line> nodeList = new ArrayList<>();
		for (Node node : XPathHelper.getNodes(doc,
				"/PcGts/Page/TextRegion/TextLine")) {
			nodeList.add(new Line(node, this));
		}
		return nodeList;
	}

	public void save(Path path) throws Exception {
		save(path.toFile());
	}

	public void save(java.io.File file) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}
}
