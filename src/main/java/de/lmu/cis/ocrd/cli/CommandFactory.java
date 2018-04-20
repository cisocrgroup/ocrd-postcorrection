package de.lmu.cis.ocrd.cli;

import java.util.HashMap;
import java.util.Optional;

public class CommandFactory {
	private final HashMap<String, Command> commands = new HashMap<String, Command>();

	public Optional<Command> get(String command) {
		if (command == null) {
			return Optional.empty();
		}
		String key = command.toLowerCase();
		if (!this.commands.containsKey(key)) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.commands.get(key));
	}

	public CommandFactory register(String commandName, Command command) {
		this.commands.put(commandName.toLowerCase(), command);
		return this;
	}

}
