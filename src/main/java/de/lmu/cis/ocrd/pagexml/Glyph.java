package de.lmu.cis.ocrd.pagexml;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class Glyph extends TextRegion {
	public Glyph(Node node) throws XPathExpressionException {
		super(node);
	}
}
