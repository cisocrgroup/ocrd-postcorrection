package de.lmu.cis.ocrd.profile;

import java.nio.file.Path;

public interface Profiler {
	Profile profile(Path path) throws Exception;
}
