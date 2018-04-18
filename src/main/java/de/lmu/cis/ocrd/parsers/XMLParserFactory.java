package de.lmu.cis.ocrd.parsers;

import org.w3c.dom.Document;

public interface XMLParserFactory {
	Parser create(Document doc, int pageID);
}
