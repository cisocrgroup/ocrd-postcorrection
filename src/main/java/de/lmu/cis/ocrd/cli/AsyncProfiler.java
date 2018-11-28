package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class AsyncProfiler implements Profiler {
	private final Future<Profile> profile;

	AsyncProfiler(Profiler profiler) {
		profile = startToProfile(profiler);
	}

	private static Future<Profile> startToProfile(Profiler profiler) {
		FutureTask<Profile> future = new FutureTask<Profile>(null);
		future.run();
		return future;
	}

	@Override
	public Profile profile(Path path) throws Exception {
		return profile.get();
	}
}
