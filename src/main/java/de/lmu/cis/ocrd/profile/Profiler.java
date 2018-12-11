package de.lmu.cis.ocrd.profile;

import java.io.Reader;

public interface Profiler {
	Profile profile(Reader r) throws Exception;
}
