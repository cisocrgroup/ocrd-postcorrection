package de.lmu.cis.ocrd.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Profiler {
    private String path = "";
    private String config = "";
    private boolean noCache = false;

    public Path getCachedPath(Path cache, String ifg, boolean alex, int n) {
        Path ifgPath = Paths.get(ifg);
        ifg = ifgPath.getFileName().toString();
        String suffix = ".json.gz";
        if (alex) {
            suffix = "_alex_" + n + suffix;
        }
        return Paths.get(cache.toString(), ifg + suffix);
    }

    public boolean isNoCache() {
        return noCache;
    }

    public void setIsNoCache(boolean noCache) {
        this.noCache = noCache;
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
