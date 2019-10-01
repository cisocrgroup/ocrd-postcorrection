package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.profile.AbstractProfiler;
import de.lmu.cis.ocrd.profile.ProfilerProcess;
import org.pmw.tinylog.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileGrpProfiler extends AbstractProfiler {
    private final List<Page> pages;

    public FileGrpProfiler(List<Page> pages, ProfilerProcess profiler) {
        super(profiler);
        this.pages = pages;
        Logger.debug("FileGrpProfiler pages: {}", pages.size());
    }

    @Override
    protected InputStream open() {
        StringBuilder b = new StringBuilder();
        for (Page page: pages) {
            for (Line line: page.getLines()) {
                for (Word word: line.getWords()) {
                    List<String> unicode = word.getUnicodeNormalized();
                    if (!unicode.isEmpty() && !unicode.get(0).isEmpty()) {
                        // Logger.debug("profiler input: {}", unicode.get(0));
                        // append master ocr (one token per line)
                        b.append(unicode.get(0));
                        b.append(":\n");
                    }
                }
            }
        }
        return new ByteArrayInputStream(b.toString().getBytes(StandardCharsets.UTF_8));
    }
}
