package de.lmu.cis.ocrd.profile;

import java.io.InputStream;
import java.io.Reader;

public abstract class AbstractProfiler implements Profiler {
    private final ProfilerProcess profiler;

    protected AbstractProfiler(ProfilerProcess profiler) {
        this.profiler = profiler;
    }

    protected abstract InputStream open() throws Exception;

    @Override
    public Profile profile() throws Exception {
        try (InputStream is = open()) {
            try (Reader r = profiler.profile(is)) {
                return Profile.read(r);
            }
        }
    }
}
