package de.lmu.cis.ocrd.cli;

class InfoCommand implements Command {
	public static final String NAME = "ocrd-profiler-autocorrection";
	public static final String PROJECT = "OCR-D";
	public static final String INSTITUTION = "CIS";

	@Override
	public void execute(Configuration config) {
		System.out.println("info:        " + NAME);
		System.out.println("project:     " + PROJECT);
		System.out.println("institution: " + INSTITUTION);
	}
}