package de.lmu.cis.ocrd.parsers;

import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.SimpleLine;

public abstract class AbstractXPathParser implements Parser {

	protected class Word {
		private final boolean explicitWhitespace;
		private final String word;
		private final ArrayList<Double> cs;

		public Word(String word) {
			this(word, false, new ArrayList<Double>());
		}

		public Word(String word, boolean explicitWhitespace) {
			this(word, explicitWhitespace, new ArrayList<Double>());
		}

		public Word(String word, boolean prepend, ArrayList<Double> cs) {
			this.word = word;
			this.explicitWhitespace = prepend;
			this.cs = cs;
		}

		public Word fillConfidences(double c) {
			this.cs.clear();
			final int n = this.word.codePointCount(0, this.word.length());
			for (int i = 0; i < n; i++) {
				cs.add(c);
			}
			return this;
		}

		public ArrayList<Double> getConfidences() {
			return this.cs;
		}

		public String getWord() {
			return this.word;
		}

		public boolean hasExplicitWhitespace() {
			return this.explicitWhitespace;
		}
	}

	protected SimpleDocument doc;

	protected final int pageID;
	protected final org.w3c.dom.Document xml;

	protected AbstractXPathParser(org.w3c.dom.Document xml, int pageID) {
		this.pageID = pageID;
		this.xml = xml;
	}

	protected abstract XPathExpression getLinesXPath();

	protected abstract XPathExpression getWordsXPath();

	@Override
	public SimpleDocument parse() throws Exception {
		NodeList lines = (NodeList) getLinesXPath().evaluate(this.xml, XPathConstants.NODESET);
		final int n = lines.getLength();
		this.doc = new SimpleDocument();
		for (int i = 0; i < n; i++) {
			parseLine(i + 1, lines.item(i));
		}
		return this.doc;
	}

	protected void parseLine(int lid, Node line) throws Exception {
		NodeList words = (NodeList) getWordsXPath().evaluate(line, XPathConstants.NODESET);
		final int n = words.getLength();
		StringBuilder str = new StringBuilder();
		ArrayList<Double> cs = new ArrayList<Double>();
		for (int i = 0; i < n; i++) {
			Word word = parseWord(words.item(i));
			if (i > 0 && !word.hasExplicitWhitespace()) {
				str.append(' ');
			}
			str.append(word.getWord());
			cs.addAll(word.getConfidences());
		}
		this.doc.add(this.pageID,
				new SimpleLine().withLineId(lid).withPageId(this.pageID).withOcr(str.toString()).withConfidences(cs));
	}

	abstract Word parseWord(Node word) throws Exception;
}
