package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class XPathHelper {

    private static final XPath xpath = XPathFactory.newInstance().newXPath();

	static final XPathExpression CHILD_TEXT_EQUIV = compile("./TextEquiv");
	static final XPathExpression CHILD_WORD = compile("./Word");
	static final XPathExpression CHILD_GLYPH_TEXT_EQUIV = compile("./Glyph/TextEquiv[@conf]");
    static final XPathExpression CHILD_TEXT_EQUIV_UNICODE = compile("./TextEquiv/Unicode");
	static final XPathExpression TEXT_LINES = compile("//TextRegion/TextLine");

	private static XPathExpression compile(String expr) {
		try {
			return xpath.compile(expr);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<Node> getNodes(Node node, XPathExpression expr) {
		try {
			final NodeList ns = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
			ArrayList<Node> list = new ArrayList<>();
			for (int i = 0; i < ns.getLength(); i++) {
				list.add(ns.item(i));
			}
			return list;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	static List<Node> getNodes(Node node, String expr) {
			return getNodes(node, compile(expr));
	}

	static Node getNode(Node node, String expr) {
			return getNode(node, compile(expr));
	}

	private static Node getNode(Node node, XPathExpression expr) {
		try {
			return (Node)expr.evaluate(node, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	static Optional<String> getAttribute(Node node, String key) {
		final Node val = node.getAttributes().getNamedItem(key);
		if (val == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(val.getNodeValue());
	}
}
