package de.lmu.cis.ocrd.ml;

public interface Feature {
	String getName();
	boolean isBoolean();
	Value calculate(Token token);
}
