package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.profile.AbstractProfiler;
import de.lmu.cis.ocrd.profile.ProfilerProcess;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BaseOCRTokenProfiler extends AbstractProfiler {
    private final BaseOCRTokenReader baseOCRTokenReader;

    public BaseOCRTokenProfiler(BaseOCRTokenReader baseOCRTokenReader, ProfilerProcess profiler) {
        super(profiler);
        this.baseOCRTokenReader = baseOCRTokenReader;
    }

    @Override
    protected InputStream open() throws Exception {
        StringBuilder b = new StringBuilder();
        boolean haveAtLeastOneToken = false;
        for (BaseOCRToken token: baseOCRTokenReader.read()) {
            b.append(token.getMasterOCR().getWordNormalized());
            b.append(":\n");
            haveAtLeastOneToken = true;
        }
        if (!haveAtLeastOneToken) {
            return null;
        }
        return new ByteArrayInputStream(b.toString().getBytes(StandardCharsets.UTF_8));
    }
}
