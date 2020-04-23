package de.lmu.cis.ocrd.cli;

import java.util.HashMap;

public class CommandFactory {
	private final HashMap<String, String> registry = new HashMap<>();

	public Command get(String command) throws Exception {
		if (command == null) {
			throw new Exception("missing command");
		}
		final String key = command.toLowerCase();
		if (!registry.containsKey(key)) {
			throw new Exception("missing command: " + key);
		}
		return (Command) Class.forName(registry.get(key)).getDeclaredConstructor().newInstance();
	}

	// Puts the lower case getName() into the registry.
	public <E extends Command> CommandFactory register(Class<E> command) throws Exception {
		registry.put(((Command) Class.forName(command.getName()).getDeclaredConstructor().newInstance()).getName().toLowerCase(), command.getName());
		return this;
	}

}
