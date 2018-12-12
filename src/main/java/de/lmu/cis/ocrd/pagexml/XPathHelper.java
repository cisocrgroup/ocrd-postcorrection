package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;

class XPathHelper {
	private static final XPath xpath = XPathFactory.newInstance().newXPath();

	public static XPathExpression compile(String expr) throws XPathExpressionException {
		return xpath.compile(expr);
	}

	public static List<Node> getNodes(Node node, XPathExpression expr) throws XPathExpressionException {
		final NodeList ns = (NodeList)expr.evaluate(node, XPathConstants.NODESET);
		ArrayList<Node> list = new ArrayList<>();
		for (int i = 0; i < ns.getLength(); i++) {
			list.add(ns.item(i));
		}
		return list;
	}

	public static List<Node> getNodes(Node node, String expr) throws XPathExpressionException {
		return getNodes(node, compile(expr));
	}

	public static String getAttribute(Node node, String key) {
		final Node val = node.getAttributes().getNamedItem(key);
		if (val == null) {
			throw new RuntimeException("no attribute " + key + " for node " + node.getNodeName());
		}
		return val.getNodeValue();
	}
}
