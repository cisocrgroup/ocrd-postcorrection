package de.lmu.cis.ocrd.cli;

public interface Command {
	void execute(Configuration config) throws Exception;
}
