package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Word extends TextRegion {
	private final List<Glyph> glyphs;
	private final Line parent;

	public Word(Node node, Line parent) throws XPathExpressionException {
		super(node);
		this.parent = parent;
		this.glyphs = getGlyphNodes(node);
	}

	public List<Glyph> getGlyphs() {
		return glyphs;
	}

	public Line getParentLine() {
		return parent;
	}

	private static List<Glyph> getGlyphNodes(Node node) throws XPathExpressionException {
		ArrayList<Glyph> glyphList = new ArrayList<>();
		for (Node n : XPathHelper.getNodes(node, "./Glyph")) {
			glyphList.add(new Glyph(n));
		}
		return glyphList;
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("|");
		final int max = 5;
		int i = 0;
		for (String str : getUnicodeNormalized()) {
			sj.add(str);
			i++;
			if (i >= max) {
				sj.add("...");
				break;
			}
		}
		return sj.toString();
	}
}
