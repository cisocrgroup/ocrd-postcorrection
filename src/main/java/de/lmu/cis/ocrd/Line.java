package de.lmu.cis.ocrd;

import java.util.List;

import de.lmu.cis.pocoweb.Token;

public interface Line {
	public int getLineId();
	public String getNormalized();
	public Token getTokenAt(int i);
	public List<Token> getTokens();
}
