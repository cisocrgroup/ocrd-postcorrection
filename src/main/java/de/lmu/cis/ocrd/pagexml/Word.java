package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.util.Unicode;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Word extends TextRegion {
	private List<Glyph> glyphs;
	private Line parent;

	public Word(Node node, Line parent) {
		super(node);
		this.parent = parent;
		this.glyphs = null; // loaded lazily
	}

	public List<Glyph> getGlyphs() {
		if (glyphs == null) {
			glyphs = getGlyphNodes(node);
		}
		return glyphs;
	}

	Line getParentLine() {
		return parent;
	}

	List<Double> getCharConfidences() {
		List<Double> confidences = new ArrayList<>();
		for (Node node: XPathHelper.getNodes(node, "./Glyph/TextEquiv[@conf]")) {
			confidences.add(Double.parseDouble(node.getAttributes().getNamedItem("conf").getNodeValue()));
		}
		return confidences;
	}

	private List<Glyph> getGlyphNodes(Node node) {
		ArrayList<Glyph> glyphList = new ArrayList<>();
		for (Node n : XPathHelper.getNodes(node, "./Glyph")) {
			glyphList.add(new Glyph(n, this));
		}
		return glyphList;
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(",");
		final List<TextEquiv> tes = getTextEquivs();
		final int max = 5; // just an arbitrary boundary
		int i = 0;
		for (TextEquiv te : tes) {
			if (i++ >= max) {
				sj.add("...");
				break;
			}
			String prefix;
			if (te.getDataType().contains("master-ocr")) {
				prefix = "master:";
			} else if (te.getDataTypeDetails().contains("-GT-")) {
				prefix = "gt:";
			} else {
				prefix = "slave:";
			}
			sj.add(prefix + te.getUnicodeNormalized());
		}
		return sj.toString();
	}

    void split(String[] words) throws Exception {
		Logger.debug("tokens: {}", String.join(" ", words));
		// do nothing if it is just one word
		if (words.length <= 1) {
			return;
		}
		List<Node> tokens = new ArrayList<>();
		int offset = 0;
		for (String word: words) {
			// skip whitespace in characters (we do not care)
			for (;offset < getGlyphs().size(); offset++) {
				if (!Unicode.isSpace(getGlyphs().get(offset).getLetters().get(0))) {
					break;
				}
			}
			int[] cps = word.codePoints().toArray();
			for (int i = 0; i < cps.length; i++) {
				if (cps[i] != getGlyphs().get(offset + i).getLetters().get(0)) {
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
			List<Coordinates> glyphCoords = new ArrayList<>(newWord.getGlyphs().size());
			for (Glyph glyph: newWord.getGlyphs()) {
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

    private Node newTokenNode(String word, int begin, int end) {
		Word newWord = new Word(node.cloneNode(true), parent);
		for (int i = 0; i < begin; i++) {
			newWord.node.removeChild(newWord.getGlyphs().get(i).node);
		}
		for (int i = end; i < getGlyphs().size(); i++) {
			newWord.node.removeChild(newWord.getGlyphs().get(i).node);
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
