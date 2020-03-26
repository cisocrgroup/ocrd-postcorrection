package de.lmu.cis.ocrd.profile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public abstract class AbstractProfiler implements Profiler {
    private final ProfilerProcess profiler;

    protected AbstractProfiler(ProfilerProcess profiler) {
        this.profiler = profiler;
    }

    protected abstract InputStream open() throws Exception;

    @Override
    public Profile profile() throws Exception {
        try (InputStream is = open()) {
            if (is == null) {
                return Profile.empty();
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                profiler.profile(is, out);
                return Profile.fromJSON(new String(out.toByteArray()));
            }
        }
    }
}
