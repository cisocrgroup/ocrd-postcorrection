package de.lmu.cis.ocrd.parsers;

import org.w3c.dom.Document;

public class ALTOXMLParserFactory implements XMLParserFactory {
	@Override
	public Parser create(Document doc, int pageID) {
		return new ALTOXMLParser(doc, pageID);
	}
}
