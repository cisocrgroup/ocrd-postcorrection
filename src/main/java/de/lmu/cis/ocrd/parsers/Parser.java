package de.lmu.cis.ocrd.parsers;

import de.lmu.cis.ocrd.SimpleDocument;

public interface Parser {
	SimpleDocument parse() throws Exception;
}
