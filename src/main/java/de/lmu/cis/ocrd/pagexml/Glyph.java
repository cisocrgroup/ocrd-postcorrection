package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class Glyph extends TextRegion {
	private Word parent;
	public Glyph(Node node, Word parent) {
		super(node);
		this.parent = parent;
	}

	List<Integer> getLetters() throws XPathExpressionException {
		List<Integer> chars = new ArrayList<>();
		for (String c: getUnicode()) {
			if (!c.isEmpty()) {
				chars.add(c.codePointAt(0));
			}
		}
		return chars;
	}
}
