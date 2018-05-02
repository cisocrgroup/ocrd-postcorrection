package de.lmu.cis.ocrd.ml;

public interface Feature {
	Value calculate(Token token);
}
