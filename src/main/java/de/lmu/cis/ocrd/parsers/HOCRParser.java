package de.lmu.cis.ocrd.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

public class HOCRParser extends AbstractXPathParser {

	private static final Pattern confidence = Pattern.compile(".*x_wconf\\s*(\\p{Digit}+).*");

	private static final XPathExpression linesXPath;

	private static final XPathExpression wordsXPath;
	static {
		try {
			linesXPath = XPathFactory.newInstance().newXPath().compile("//span[@class=\"ocr_line\"]");
			wordsXPath = XPathFactory.newInstance().newXPath().compile("./span[@class=\"ocrx_word\"]");
		} catch (XPathException e) {
			throw new RuntimeException(e);
		}
	}

	private static double getConfidence(Node word) {
		if (word.getAttributes() == null) {
			return 0;
		}
		if (word.getAttributes().getNamedItem("title") == null) {
			return 0;
		}
		Matcher m = confidence.matcher(word.getAttributes().getNamedItem("title").getNodeValue());
		if (!m.matches()) {
			return 0;
		}
		return (double) 1 / Integer.parseInt(m.group(1));
	}

	public HOCRParser(org.w3c.dom.Document xml, int pageID) {
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
			return new Word("", true);
		}
		return new Word(word.getFirstChild().getNodeValue(), false).fillConfidences(getConfidence(word));
	}
}
