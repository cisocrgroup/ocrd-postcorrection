package de.lmu.cis.ocrd.parsers;

import de.lmu.cis.ocrd.archive.Archive;

public class OcropusArchiveGTParser extends OcropusArchiveParser {
	public OcropusArchiveGTParser(Archive ar) {
		super(ar, new OcropusGTFileType());
	}
}
