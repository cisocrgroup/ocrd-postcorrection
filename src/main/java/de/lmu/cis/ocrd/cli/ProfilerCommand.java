package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;

import java.util.List;

public class ProfilerCommand extends AbstractIOCommand {
	public static class Parameter {
		public String executable, backend, language;
		public List<String> args;
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		final Parameter parameter = config.mustGetParameter(Parameter.class);
		final Profiler profiler = makeLocalProfiler(parameter);
		final Profile profile = profiler.getProfile();
		println(new Gson().toJson(profile));
		flush();
	}

	@Override
	public String getName() {
		return "profile";
	}

	private Profiler makeLocalProfiler(Parameter parameter) throws Exception {
		return new LocalProfiler()
				.withExecutable(parameter.executable)
				.withLanguageDirectory(parameter.backend)
				.withLanguage(parameter.language)
				.withArgs(parameter.args.toArray(new String[parameter.args.size()]));
	}
}
