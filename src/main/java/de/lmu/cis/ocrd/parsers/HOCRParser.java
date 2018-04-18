package de.lmu.cis.ocrd.parsers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.SimpleLine;

public class HOCRParser implements Parser {

	private static final Pattern confidence = Pattern.compile(".*x_wconf\\s*(\\p{Digit}+).*");

	private static final XPathExpression linesXPath;

	private static final XPathExpression wordsXPath;
	static {
		try {
			linesXPath = XPathFactory.newInstance().newXPath().compile("./span[@class=ocr_line]");
			wordsXPath = XPathFactory.newInstance().newXPath().compile("./span[@class=ocrx_word]");
		} catch (XPathException e) {
			throw new RuntimeException(e);
		}
	}

	private static double getConfidence(Node word) {
		if (word.getAttributes() == null) {
			return 0;
		}
		if (word.getAttributes().getNamedItem("title") == null) {
			return 0;
		}
		Matcher m = confidence.matcher(word.getAttributes().getNamedItem("title").getNodeValue());
		if (!m.matches()) {
			return 0;
		}
		return (double) 1 / Integer.parseInt(m.group(1));
	}

	private static String getWord(Node word) throws Exception {
		Node data = word.getFirstChild();
		if (data == null) {
			throw new Exception("invalid span class=ocrx_word element encountered: missing text node");
		}
		if (data.getNodeType() != Node.TEXT_NODE) {
			throw new Exception("invalid span class=ocrx_word element encountered: missing text node");
		}
		String w = data.getNodeValue();
		if (w == null) {
			throw new Exception("invalid span class=ocrx_word element encountered: null string");
		}
		return w;
	}

	private final org.w3c.dom.Document xml;

	private SimpleDocument doc;
	private final int pageid;

	public HOCRParser(org.w3c.dom.Document xml, int pageid) {
		this.xml = xml;
		this.pageid = pageid;
	}

	@Override
	public SimpleDocument parse() throws Exception {
		NodeList lines = (NodeList) linesXPath.evaluate(this.xml, XPathConstants.NODESET);
		final int n = lines.getLength();
		for (int i = 0; i < n; i++) {
			parseLine(i + 1, lines.item(i));
		}
		return this.doc;
	}

	private void parseLine(int lid, Node line) throws Exception {
		NodeList words = (NodeList) wordsXPath.evaluate(this.xml, XPathConstants.NODESET);
		final int n = words.getLength();
		StringBuilder str = new StringBuilder();
		ArrayList<Double> cs = new ArrayList<Double>();
		for (int i = 0; i < n; i++) {
			if (i > 0) {
				str.append(' ');
			}
			final String w = getWord(words.item(i));
			final double c = getConfidence(words.item(i));
			final int m = w.codePointCount(0, w.length());
			for (int j = 0; j < m; j++) {
				cs.add(c);
			}
			str.append(getWord(words.item(i)));

		}
		doc.add(this.pageid,
				new SimpleLine().withLineId(lid).withPageId(this.pageid).withOcr(str.toString()).withConfidences(cs));
	}
}
