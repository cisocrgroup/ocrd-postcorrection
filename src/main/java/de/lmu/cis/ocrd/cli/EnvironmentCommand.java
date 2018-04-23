package de.lmu.cis.ocrd.cli;

public class EnvironmentCommand implements Command {
	@Override
	public void execute(Configuration config) throws Exception {
		for (String opt : config.getCommandLine().getArgs()) {
			System.out.println(opt + ": " + config.getCommandLine().getOptionValue(opt));
		}
	}
}
