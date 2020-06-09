package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Page {
	public static final String MIME_TYPE = "application/vnd.prima.page+xml";
	public static final String DATA_TYPE = "OCR-D-CIS-POST-CORRECTION";

	private final Document doc;
	private final Path path;
	private METS.File metsFile;

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

	private List<Line> lines;

	public Page(Path path, Document doc) {
		this.path = path;
		this.doc = doc;
		this.lines = null;//getLineNodes(doc);
	}

	public Path getPath() {
		return path;
	}

	Node getRoot() {return doc;}

	public List<Line> getLines() {
		if (lines == null) {
			lines = getLineNodes(doc);
		}
		return this.lines;
	}

	public List<TextRegion> getTextRegions() {
		ArrayList<TextRegion> nodeList = new ArrayList<>();
		for (Node node : XPathHelper.getNodes(doc, "/PcGts/Page/TextRegion")) {
			nodeList.add(new TextRegion(node));
		}
		return nodeList;
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

	public void save(File file) throws Exception {
		if (!file.createNewFile()) {
			if (!file.delete()) {
				throw new Exception("cannot overwrite file: " + file.getPath());
			}
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}

	public void setMetsFile(METS.File file) {
		this.metsFile = file;
	}

	public METS.File getMetsFile() {
		return this.metsFile;
	}

    public void correctLinesAndRegions() throws XPathExpressionException {
		for (Node textRegionNode: XPathHelper.getNodes(doc, "/PcGts/Page/TextRegion")) {
			TextRegion textRegion = new TextRegion(textRegionNode);
			List<String> linesInRegion = new ArrayList<>();
			for (Node textLineNode: XPathHelper.getNodes(textRegionNode,"./TextLine")) {
				List<String> wordsInLine = new ArrayList<>();
				Line textLine = new Line(textLineNode, this);
				for (Word word: textLine.getWords()) {
					wordsInLine.add(word.getUnicode().get(0));
				}
				String lineStr = String.join(" ", wordsInLine);
				textLine.incrementTextEquivIndices();
				textLine.prependNewTextEquiv()
						.withIndex(1)
						.withDataType(DATA_TYPE)
						.addUnicode(lineStr);
				linesInRegion.add(lineStr);
			}
			String regionStr = String.join("\n", linesInRegion);
			textRegion.incrementTextEquivIndices();
			textRegion.prependNewTextEquiv()
					.withIndex(1)
					.withDataType(DATA_TYPE)
					.addUnicode(regionStr);
		}
    }
}
