package de.lmu.cis.ocrd.profile;

import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TokenReaderProfiler extends AbstractProfiler {
    private final TokenReader tokenReader;

    public TokenReaderProfiler(TokenReader tokenReader, ProfilerProcess profiler) throws Exception {
        super(profiler);
        this.tokenReader = tokenReader;
    }

    @Override
    protected InputStream open() throws Exception {
        StringBuilder b = new StringBuilder();
        for (OCRToken token: tokenReader.readTokens()) {
            b.append(token.getMasterOCR().toString());
            b.append(":\n");
        }
        return new ByteArrayInputStream(b.toString().getBytes(StandardCharsets.UTF_8));
    }
}
