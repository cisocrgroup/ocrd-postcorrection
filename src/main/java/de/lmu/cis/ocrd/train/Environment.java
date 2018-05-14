package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.FeatureSet;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Environment {
    private static final String resources = "resources";
    private static final String dLex = "dLex";
    private static final String dLexFS = "features.ser";
    private final Path path;
    private Path gt, masterOCR, featureSet;
    private final List<Path> otherOCR = new ArrayList<>();
    private boolean copyTrainingFiles;

    public Environment(String base, String name) throws IOException {
        this.path = Paths.get(base, name);
        setupDirectories();
        setupTrainingDirectories(1);
    }

    private void setupDirectories() throws IOException {
        Files.createDirectory(path);
        Files.createDirectory(getResourcesDirectory());
        Files.createDirectory(getDynamicLexiconTrainingDirectory());
    }

    private void setupTrainingDirectories(int n) throws IOException {
        Files.createDirectory(getDynamicLexiconTrainingDirectory(n));
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
        if (!copyTrainingFiles) {
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
        if (!copyTrainingFiles) {
            this.masterOCR = Paths.get(masterOCR);
        } else {
            this.masterOCR = copy(Paths.get(masterOCR));
        }
        return this;
    }

    public Environment addOtherOCR(String otherOCR) throws IOException {
       if (!copyTrainingFiles) {
            this.otherOCR.add(Paths.get(otherOCR));
       } else {
           this.otherOCR.add(copy(Paths.get(otherOCR)));
       }
       setupTrainingDirectories(1 + this.otherOCR.size());
       return this;
    }

    public Path getOtherOCR(int i) {
        return otherOCR.get(i);
    }

    public int getNumberOfOtherOCR() {
        return otherOCR.size();
    }

    public Environment withCopyTrainingFiles(boolean copy) {
        this.copyTrainingFiles = copy;
        return this;
    }

    public Environment withDynamicLexiconFeatureSet(FeatureSet fs) throws IOException {
        serializeFeatureSet(fs, getDynamicLexiconFeatureSet());
        return this;
    }

    public FeatureSet loadDynamicLexiconFeatureSet() throws IOException, ClassNotFoundException {
        return deSerializeFeatureSet(getDynamicLexiconFeatureSet());
    }

    private static void serializeFeatureSet(FeatureSet fs, Path path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            out.writeObject(fs);
        }
    }

    private static FeatureSet deSerializeFeatureSet(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (FeatureSet) in.readObject();
        }
    }

    public void remove() throws IOException {
        FileUtils.deleteDirectory(path.toFile());
    }

    private Path copy(Path path) throws IOException {
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

    public Path getDynamicLexiconTrainingDirectory() {
        return Paths.get(path.toString(), dLex);
    }

    public Path getDynamicLexiconTrainingDirectory(int n) {
        return Paths.get(getDynamicLexiconTrainingDirectory().toString(), Integer.toString(n));
    }

    public Path getDynamicLexiconFeatureSet() {
        return Paths.get(getDynamicLexiconTrainingDirectory().toString(), dLexFS);
    }
}
