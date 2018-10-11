package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class Word extends TextRegion {
	private final List<Glyph> glyphs;
	
	public Word(Node node) throws XPathExpressionException {
		super(node);
		this.glyphs = getGlyphNodes(node);
	}

	public List<Glyph> getGlyphs() {
		return glyphs;
	}

	private static List<Glyph> getGlyphNodes(Node node) throws XPathExpressionException {
		ArrayList<Glyph> glyphList = new ArrayList<>();
		for (Node n : Util.getNodes(node, "./Glyph")) {
			glyphList.add(new Glyph(n));
		}
		return glyphList;
	}
}
