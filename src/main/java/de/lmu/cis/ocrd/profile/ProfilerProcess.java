package de.lmu.cis.ocrd.profile;

import java.io.InputStream;
import java.io.Reader;

public interface ProfilerProcess {
    Reader profile(InputStream is) throws Exception;
}
