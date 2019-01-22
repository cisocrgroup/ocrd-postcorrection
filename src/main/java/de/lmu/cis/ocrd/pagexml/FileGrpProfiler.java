package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.profile.AbstractProfiler;
import de.lmu.cis.ocrd.profile.ProfilerProcess;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileGrpProfiler extends AbstractProfiler {
    private List<METS.File> files;

    public FileGrpProfiler(List<METS.File> files, ProfilerProcess profiler) {
        super(profiler);
        this.files = files;
    }

    @Override
    protected InputStream open() throws Exception {
        StringBuilder b = new StringBuilder();
        for (METS.File file: files) {
            try (InputStream is = file.open()) {
                Page page = Page.parse(is);
                for (Line line: page.getLines()) {
                    for (Word word: line.getWords()) {
                        List<String> unicode = word.getUnicodeNormalized();
                        if (!unicode.isEmpty()) {
                            // append master ocr
                            b.append(unicode.get(0));
                        }
                    }
                }
            }
        }
        return new ByteArrayInputStream(b.toString().getBytes(StandardCharsets.UTF_8));
    }
}
