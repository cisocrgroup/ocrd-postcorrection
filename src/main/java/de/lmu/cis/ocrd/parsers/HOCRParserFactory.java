package de.lmu.cis.ocrd.parsers;

import org.w3c.dom.Document;

public class HOCRParserFactory implements XMLParserFactory {

	@Override
	public Parser create(Document doc, int pageID) {
		return new HOCRParser(doc, pageID);
	}
}
