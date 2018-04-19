package de.lmu.cis.ocrd.parsers;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ALTOXMLParser extends AbstractXPathParser {

	private static final XPathExpression linesXPath;

	private static final XPathExpression wordsXPath;

	static {
		try {
			linesXPath = XPathFactory.newInstance().newXPath().compile("//TextLine");
			wordsXPath = XPathFactory.newInstance().newXPath().compile("./String");
		} catch (XPathException e) {
			throw new RuntimeException(e);
		}
	}

	protected ALTOXMLParser(Document xml, int pageID) {
		super(xml, pageID);
	}

	@Override
	protected XPathExpression getLinesXPath() {
		return linesXPath;
	}

	@Override
	protected XPathExpression getWordsXPath() {
		return wordsXPath;
	}

	@Override
	Word parseWord(Node word) throws Exception {
		if (word.getAttributes() == null) {
			throw new Exception("missing attributes for alto string node");
		}
		return new Word(word.getAttributes().getNamedItem("CONTENT").getNodeValue(), false).fillConfidences(0);
	}
}
