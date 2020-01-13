package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.profile.AbstractProfiler;
import de.lmu.cis.ocrd.profile.ProfilerProcess;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class WordReaderProfiler extends AbstractProfiler {
    private final WordReader wordReader;

    public WordReaderProfiler(WordReader wordReader, ProfilerProcess profiler) {
        super(profiler);
        this.wordReader = wordReader;
    }

    @Override
    protected InputStream open() throws Exception {
        StringBuilder b = new StringBuilder();
        for (Word word : wordReader.readWords()) {
            b.append(word.toString());
            b.append(":\n");
        }
        return new ByteArrayInputStream(b.toString().getBytes(StandardCharsets.UTF_8));
    }
}
