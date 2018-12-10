package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.profile.LocalProfiler;

public class ProfilerCommand implements Command {
	public static class Parameter {
		public String executable, backend, language;
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		final Parameter parameter = config.mustGetParameter(Parameter.class);
		try (final Profile profile =
				     new Profile(makeLocalProfiler(parameter))) {
			profile.run();
		}
	}

	@Override
	public String getName() {
		return "profile";
	}

	private static LocalProfiler makeLocalProfiler(Parameter parameter) {
		return new LocalProfiler()
				.withExecutable(parameter.executable)
				.withLanguageDirectory(parameter.backend)
				.withLanguage(parameter.language)
				.withArgs("--types");
	}
}
