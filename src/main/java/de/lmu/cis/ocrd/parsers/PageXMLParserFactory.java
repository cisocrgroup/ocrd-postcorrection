package de.lmu.cis.ocrd.parsers;

import org.w3c.dom.Document;

public class PageXMLParserFactory implements XMLParserFactory {
	@Override
	public Parser create(Document doc, int pageID) {
		return new PageXMLParser(doc, pageID);
	}
}
