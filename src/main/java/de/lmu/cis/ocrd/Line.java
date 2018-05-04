package de.lmu.cis.ocrd;

import de.lmu.cis.pocoweb.Token;

import java.util.List;

public interface Line {
	int getLineId();

	int getPageId();

	String getNormalized();

	List<Token> getTokens();
}
