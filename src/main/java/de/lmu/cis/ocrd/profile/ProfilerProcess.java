package de.lmu.cis.ocrd.profile;

import java.io.InputStream;
import java.io.OutputStream;

public interface ProfilerProcess {
    void profile(InputStream is, OutputStream os) throws Exception;
}
