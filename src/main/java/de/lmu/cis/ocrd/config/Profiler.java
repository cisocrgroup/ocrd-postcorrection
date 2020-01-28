package de.lmu.cis.ocrd.config;

import de.lmu.cis.ocrd.profile.AdditionalLexicon;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Profiler {
    private String path = "";
    private String config = "";

    public Path getCachedPath(Path cache, String ifg, AdditionalLexicon alex, int n) {
        String suffix = ".json.gz";
        if (alex.use()) {
            suffix = "_alex_" + n + suffix;
        }
        return Paths.get(cache.toString(), ifg + suffix);
    }

    public Path getPath() {
        return Paths.get(path);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Path getConfig() {
        return Paths.get(config);
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
