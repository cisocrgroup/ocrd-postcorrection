package de.lmu.cis.ocrd.ml;

import de.lmu.cis.pocoweb.Token;

public interface Feature {
	Value calculate(Token token);
}
