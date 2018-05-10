package de.lmu.cis.ocrd.train;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Environment {
    private boolean copyData;
    private final Path path;

    public Environment(String base, String name) throws IOException {
        this.path = Paths.get(base, name);
        Files.createDirectory(path);
    }

    public String getName() {
        return path.getFileName().toString();
    }

    public Path getPath() {
        return path;
    }


    public Environment withCopyData(boolean copyData) {
        this.copyData = copyData;
        return this;
    }

    public void remove() throws IOException {
        FileUtils.deleteDirectory(path.toFile());
    }
}
