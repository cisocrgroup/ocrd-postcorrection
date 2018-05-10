package de.lmu.cis.ocrd.train;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Environment {
    private static final String resources = "resources";
    private final Path path;
    private Path gt, masterOCR;
    private final List<Path> otherOCR = new ArrayList<>();
    private boolean copyData;

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

    public Path getGT() {
        return gt;
    }

    public Environment withGT(String gt) throws IOException {
        if (!copyData) {
            this.gt = Paths.get(gt);
        } else {
            this.gt = copy(Paths.get(gt));
        }
        return this;
    }

    public Path getMasterOCR() {
        return masterOCR;
    }

    public Environment withMasterOCR(String masterOCR) throws IOException {
        if (!copyData) {
            this.masterOCR = Paths.get(masterOCR);
        } else {
            this.masterOCR = copy(Paths.get(masterOCR));
        }
        return this;
    }

    public Environment addOtherOCR(String otherOCR) throws IOException {
       if (!copyData) {
            this.otherOCR.add(Paths.get(otherOCR));
       } else {
           this.otherOCR.add(copy(Paths.get(otherOCR)));
       }
       return this;
    }

    public Path getOtherOCR(int i) {
        return otherOCR.get(i);
    }

    public int getNumberOfOtherOCR() {
        return otherOCR.size();
    }

    public Environment withCopyData(boolean copyData) {
        this.copyData = copyData;
        return this;
    }

    public void remove() throws IOException {
        FileUtils.deleteDirectory(path.toFile());
    }

    private Path copy(Path path) throws IOException {
        if (!Files.exists(getResourcesDirectory())) {
            getResourcesDirectory().toFile().mkdirs();
        }
        final Path target = Paths.get(getResourcesDirectory().toString(), path.getFileName().toString());
        if (Files.isDirectory(path)) {
            FileUtils.copyDirectory(path.toFile(), target.toFile());
        } else {
            FileUtils.copyFile(path.toFile(), target.toFile());
        }
        return target;
    }

    public Path getResourcesDirectory() {
        return Paths.get(path.toString(), resources);
    }

}
