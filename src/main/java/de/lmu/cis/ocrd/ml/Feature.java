package de.lmu.cis.ocrd.ml;

public interface Feature {
	String getName();
	double calculate(Token token);
}
