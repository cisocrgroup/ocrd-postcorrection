package de.lmu.cis.ocrd.parsers;

import java.io.StringReader;

// Simple text parser. Mostly for testing purposes.
public class StringParser extends TextParser {
	public StringParser(int pageID, String page) {
		super(pageID, new StringReader(page));
	}
}
