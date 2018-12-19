package de.lmu.cis.ocrd.train.step;

import java.nio.file.Path;

import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profiler;
import de.lmu.cis.ocrd.profile.ProfilerBuilder;

public class LocalProfilerBuilder implements ProfilerBuilder {
	private final Config config;
	private final Path output;

	public LocalProfilerBuilder(Config config, Path output) {
		this.config = config;
		this.output = output;
	}

	@Override
	public Profiler build() {
		return new LocalProfiler().withExecutable(config.profiler)
			.withLanguageDirectory(config.profilerLanguageDir)
			.withLanguage(config.profilerLanguage);
	}
}
