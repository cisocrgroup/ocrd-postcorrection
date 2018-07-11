package de.lmu.cis.ocrd.parsers;

import org.w3c.dom.Document;

public class ABBYYXMLParserFactory implements XMLParserFactory {
	@Override
	public Parser create(Document doc, int pageID) {
		return new ABBYYXMLParser(doc, pageID);
	}
}
