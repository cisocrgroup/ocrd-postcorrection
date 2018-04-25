package de.lmu.cis.ocrd.cli;

interface Command {
	void execute(Configuration config) throws Exception;
}
