package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class Util {
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
}
