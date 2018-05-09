package de.lmu.cis.ocrd.cli;

public class InvalidCommand implements Command {
	private final String command;

	public InvalidCommand(String command) {
		this.command = command;
	}

	@Override
	public String getName() {
		return "invalid";
	}

	@Override
	public void execute(Configuration config) throws Exception {
		throw new Exception("invalid command: " + this.command);
	}

}
