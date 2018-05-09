package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.Token;

public interface Feature {
	String getName();
	double calculate(Token token);
}
