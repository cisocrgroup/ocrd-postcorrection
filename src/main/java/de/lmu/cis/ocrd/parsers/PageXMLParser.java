package de.lmu.cis.ocrd.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class PageXMLParser extends AbstractXPathParser {
	private static final XPathExpression linesXPath;

	private static final XPathExpression wordsXPath;

	static {
		try {
			linesXPath = XPathFactory.newInstance().newXPath().compile("//TextLine");
			wordsXPath = XPathFactory.newInstance().newXPath().compile("./Word/TextEquiv/Unicode");
		} catch (XPathException e) {
			throw new RuntimeException(e);
		}
	}

	PageXMLParser(Document xml, int pageID) {
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
		if (word.getFirstChild() == null || word.getFirstChild().getNodeType() != Node.TEXT_NODE) {
			throw new Exception("invalid page XML word node: missing unicode content");
		}
		return new Word(word.getFirstChild().getNodeValue(), false).fillConfidences(0);
	}

}
