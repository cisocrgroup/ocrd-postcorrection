package de.lmu.cis.ocrd.cli;

import java.util.HashMap;

public class CommandFactory {
	private final HashMap<String, Command> commands = new HashMap<String, Command>();

	// Never returns null.
	public Command get(String command) {
		if (command == null) {
			return new InvalidCommand("missing command");
		}
		String key = command.toLowerCase();
		if (!this.commands.containsKey(key)) {
			return new InvalidCommand(key.toLowerCase());
		}
		return this.commands.get(key.toLowerCase());
	}

	public CommandFactory register(String commandName, Command command) {
		this.commands.put(commandName.toLowerCase(), command);
		return this;
	}

}
