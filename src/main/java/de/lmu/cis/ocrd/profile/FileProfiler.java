package de.lmu.cis.ocrd.profile;

import java.io.IOException;
import java.nio.file.Path;

// Simple Profiler that reads a profiler from a given json profile file.
public class FileProfiler implements Profiler {
    private final Profile profile;

    public FileProfiler(Path path) throws IOException {
        profile = Profile.read(path);
    }

    @Override
    public Profile profile() {
        return profile;
    }
}
