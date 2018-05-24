package de.lmu.cis.ocrd.cli;

public class InfoCommand implements Command {
	private static final String NAME = "ocrd-profiler-autocorrection";
	private final String PROJECT = "OCR-D";
	private final String INSTITUTION = "CIS";

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public void execute(CommandLineArguments config) {
		System.out.println("info:        " + NAME);
		System.out.println("project:     " + PROJECT);
		System.out.println("institution: " + INSTITUTION);
	}
}
