package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class AsyncProfiler implements Profiler {
    private final Future<Profile> profile;

    AsyncProfiler(Profiler profiler) {
        profile = startToProfile(profiler);
    }

    @Override
    public Profile profile() throws Exception {
        return profile.get();
    }

    private static Future<Profile> startToProfile(Profiler profiler) {
        FutureTask<Profile> future = new FutureTask<>(profiler::profile);
        future.run();
        return future;
    }
}
