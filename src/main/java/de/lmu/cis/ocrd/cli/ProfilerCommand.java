package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;

public class ProfilerCommand extends AbstractIOCommand {
	public static class Parameter {
		public String executable, backend, language;
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		final Parameter parameter = config.mustGetParameter(Parameter.class);
		final Profile profile =
				makeLocalProfiler(parameter).profile(getStdin());
		println(new Gson().toJson(profile));
		flush();
	}

	@Override
	public String getName() {
		return "profile";
	}

	private Profiler makeLocalProfiler(Parameter parameter) {
		return new LocalProfiler()
				.withExecutable(parameter.executable)
				.withLanguageDirectory(parameter.backend)
				.withLanguage(parameter.language)
				.withArgs("--types");
	}
}
