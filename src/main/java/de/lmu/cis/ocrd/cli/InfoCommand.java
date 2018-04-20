package de.lmu.cis.ocrd.cli;

class InfoCommand implements Command {
	public static final String NAME = "ocrd_profiler_autocorrection";
	public static final String PROJECT = "OCR-D";
	public static final String INSTITUTION = "CIS";

	@Override
	public void execute() {
		System.out.println("info:        " + NAME);
		System.out.println("project:     " + PROJECT);
		System.out.println("institution: " + INSTITUTION);
	}
}
