package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.pagexml.FileGroupProfiler;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.profile.*;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class ConfigProfiler implements Profiler {
    public String path = "", config = "", cacheDir = "";
    private AdditionalLexicon alex;
    private List<Page> pages;
    private String ifg;

    public void setAlex(AdditionalLexicon alex) {
        this.alex = alex;
    }

    void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public void setInputFileGroup(String ifg) {
        this.ifg = ifg;
    }

    @Override
    public Profile profile() throws Exception {
        if (haveCacheDir() && getCachePath().toFile().exists()) {
            Logger.debug("loading cached profile from {}", getCachePath());
            return new FileProfiler(getCachePath()).profile();
        }
        final Profile profile = getProfiler().profile();
        if (haveCacheDir()) {
            Logger.debug("caching profile to {}", getCachePath());
            getCachePath().getParent().toFile().mkdirs();
            Charset utf8 = StandardCharsets.UTF_8;
            try (Writer w = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(getCachePath().toFile())), utf8))) {
                w.write(profile.toJSON());
                w.write('\n');
            }
        }
        return profile;
    }

    private boolean haveCacheDir() {
        return !"".equals(cacheDir);
    }

    public Path getCachePath() {
        String suffix = ".json.gz";
        if (alex.use()) {
            suffix = "_" + alex.toString() + suffix;
        }
        return Paths.get(cacheDir, ifg + suffix);
    }

    private Profiler getProfiler() throws Exception {
        if (path.toLowerCase().endsWith(".json") || path.toLowerCase().endsWith(".gz")) {
            Logger.debug("using a file profiler: {}", path);
            return new FileProfiler(Paths.get(path));
        }
        if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
            throw new Exception("profiler type url: not implemented");
        }
        Logger.debug("using a local profiler: {} {}", path, config);
        return new FileGroupProfiler(pages, new LocalProfilerProcess(Paths.get(path), Paths.get(config), alex));
    }
}
