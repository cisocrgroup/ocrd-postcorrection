package de.lmu.cis.ocrd.profile;

import java.io.Reader;

public interface Profiler {
	void profile(Reader r) throws Exception;
	Profile getProfile() throws Exception;
}
