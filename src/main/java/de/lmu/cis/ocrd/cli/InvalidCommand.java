package de.lmu.cis.ocrd.cli;

class InvalidCommand implements Command {
	private final String command;

	public InvalidCommand(String command) {
		this.command = command;
	}

	@Override
	public void execute(Configuration config) throws Exception {
		throw new Exception("invalid command: " + this.command);
	}

}
