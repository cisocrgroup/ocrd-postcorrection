package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.Unicode;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Word extends TextRegion {
	private List<Glyph> glyphs;
	private final Line parent;

	public Word(Node node, Line parent) throws XPathExpressionException {
		super(node);
		this.parent = parent;
		this.glyphs = getGlyphNodes(node);
	}

	public List<Glyph> getGlyphs() {
		return glyphs;
	}

	Line getParentLine() {
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
		final int max = 9; // 3 OCRs + 1 GT + 5 Candidates
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

    void split(String[] words) throws Exception {
		Logger.debug("tokens: {}", String.join(" ", words));
		// do nothing if it is just one word
		if (words == null || words.length <= 1) {
			return;
		}
		List<Node> tokens = new ArrayList<>();
		int offset = 0;
		for (String word: words) {
			// skip whitespace in characters (we do not care)
			for (;offset < glyphs.size(); offset++) {
				if (!Unicode.isSpace(glyphs.get(offset).getLetters().get(0))) {
					break;
				}
			}
			int[] cps = word.codePoints().toArray();
			for (int i = 0; i < cps.length; i++) {
				if (cps[i] != glyphs.get(offset + i).getLetters().get(0)) {
					throw new Exception("word and glyphs out of sync");
				}
			}
			tokens.add(newTokenNode(word, offset, offset + cps.length));
			offset += cps.length;
		}
		replaceSelfWith(tokens);
    }

    private void replaceSelfWith(List<Node> tokens) throws Exception {
		List<Word> newWords = new ArrayList<>(tokens.size());
		for (int id = 0; id < tokens.size(); id++) {
			final Word newWord = new Word(tokens.get(id), parent);
			// word id
			final String newID = getID() + String.format("_%04d", id+1);
			newWord.withID(newID);
			// word coordinates
			List<Coordinates> glyphCoords = new ArrayList<>(newWord.glyphs.size());
			for (Glyph glyph: newWord.glyphs) {
				glyphCoords.add(glyph.getCoordinates());
			}
			newWord.withCoordinates(Coordinates.fromCoordinates(glyphCoords));
			newWords.add(newWord);
		}
		// insert new word node and remove this word node
		for (Word newWord: newWords) {
			parent.node.insertBefore(newWord.node, this.node);
		}
		parent.node.removeChild(this.node); // remove self from line
		// "fix" the parent line's Word list
		List<Word> newLine = new ArrayList<>();
		for (Word word: parent.words) {
			if (word != this) {
				newLine.add(word);
			} else {
				newLine.addAll(newWords);
			}
		}
		parent.words = newLine;
	}

    private Node newTokenNode(String word, int begin, int end) throws XPathExpressionException {
		Word newWord = new Word(node.cloneNode(true), parent);
		for (int i = 0; i < begin; i++) {
			newWord.node.removeChild(newWord.glyphs.get(i).node);
		}
		for (int i = end; i < glyphs.size(); i++) {
			newWord.node.removeChild(newWord.glyphs.get(i).node);
		}
		// fix text equivs
		Node te = XPathHelper.getNode(newWord.node, "./TextEquiv");
		for (Node u: XPathHelper.getNodes(te, "./Unicode")) {
			te.removeChild(u);
		}
		Element u = te.getOwnerDocument().createElement("Unicode");
		u.appendChild(u.getOwnerDocument().createTextNode(word));
		te.appendChild(u);
		newWord.node.appendChild(te);
		return newWord.node;
	}
}
