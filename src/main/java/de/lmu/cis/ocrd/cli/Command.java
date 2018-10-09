package de.lmu.cis.ocrd.cli;

public interface Command {
	void execute(CommandLineArguments config) throws Exception;

	String getName();
}
