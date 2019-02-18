package de.lmu.cis.ocrd.pagexml;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class Glyph extends TextRegion {
	public Glyph(Node node) throws XPathExpressionException {
		super(node);
	}

	public List<Integer> getLetters() {
		List<Integer> chars = new ArrayList<>();
		for (String c: getUnicode()) {
			if (!c.isEmpty()) {
				chars.add(c.codePointAt(0));
			}
		}
		return chars;
	}
}
