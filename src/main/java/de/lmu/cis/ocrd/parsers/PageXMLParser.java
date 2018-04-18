package de.lmu.cis.ocrd.parsers;

import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PageXMLParser extends AbstractXPathParser {

	protected PageXMLParser(Document xml, int pageID) {
		super(xml, pageID);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected XPathExpression getLinesXPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected XPathExpression getWordsXPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Word parseWord(Node word) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
