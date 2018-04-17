package de.lmu.cis.ocrd;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ABBYYPageParser {
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

	private final org.w3c.dom.Document xml;

	private final int pageid;

	private SimpleDocument doc;

	public ABBYYPageParser(org.w3c.dom.Document xml, int pageid) {
		this.xml = xml;
		this.pageid = pageid;
	}

	public SimpleDocument parse() throws Exception {
		NodeList lines = xml.getElementsByTagName("line");
		final int n = lines.getLength();
		this.doc = new SimpleDocument();
		for (int i = 0; i < n; i++) {
			parseLine(i + 1, lines.item(i));
		}
		return this.doc;
	}

	private void parseLine(int lid, Node line) throws Exception {
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
		SimpleLine tmp = new SimpleLine().withOcr(str.toString()).withConfidences(cs).withLineId(lid)
				.withPageId(this.pageid);
		this.doc.add(this.pageid, tmp);
	}

}
