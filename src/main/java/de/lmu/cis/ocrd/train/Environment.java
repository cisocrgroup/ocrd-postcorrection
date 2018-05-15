package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.DirectoryArchive;
import de.lmu.cis.ocrd.archive.ZipArchive;
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
    private static final String trainingFile = "training.arff";
    private final Path path;
    private Path gt, masterOCR;
    private final List<Path> otherOCR = new ArrayList<>();
    private boolean copyTrainingFiles;
    private boolean debugTokenAlignment;

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

    public Archive loadGT() throws IOException {
        return loadArchive(getGT());
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

    public Archive loadMasterOCR() throws IOException {
        return loadArchive(getMasterOCR());
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

    public Archive loadOtherOCR(int i) throws IOException {
        return loadArchive(getOtherOCR(i));
    }

    public int getNumberOfOtherOCR() {
        return otherOCR.size();
    }

    public Environment withCopyTrainingFiles(boolean copy) {
        this.copyTrainingFiles = copy;
        return this;
    }

    public Environment withDebugTokenAlignment(boolean debug) {
        this.debugTokenAlignment = debug;
        return this;
    }

    public boolean isDebugTokenAlignment() {
        return debugTokenAlignment;
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

    private static Archive loadArchive(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            return new DirectoryArchive(path.toString());
        } else {
            return new ZipArchive(path.toString());
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

    private Configuration newConfiguration() {
        Configuration c = new Configuration();
        c.gt = gt.toString();
        c.masterOCR = masterOCR.toString();
        c.dynamicLexiconFeatureSet = getDynamicLexiconFeatureSet().toString();
        c.dynamicLexiconTrainingFiles = new String[1+otherOCR.size()];
        c.dynamicLexiconTrainingFiles[0] = getDynamicLexiconTrainingFile(1).toString();
        c.otherOCR = new String[otherOCR.size()];
        for (int i = 0; i < otherOCR.size(); i++) {
            c.dynamicLexiconTrainingFiles[i+1] = getDynamicLexiconTrainingFile(i+2).toString();
            c.otherOCR[i] = otherOCR.get(i).toString();
        }
        c.copyTrainingFiles = this.copyTrainingFiles;
        c.debugTokenAlignment = this.debugTokenAlignment;
        return c;
    }

    public Configuration loadConfiguration() throws IOException {
        return Configuration.fromJSON(getConfigurationFile());
    }

    public void writeConfiguration() throws IOException {
        try (PrintWriter out = new PrintWriter(getConfigurationFile().toFile())) {
            out.println(newConfiguration().toJSON());
        }
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

    public Path getDynamicLexiconTrainingFile(int n) {
        return Paths.get(getDynamicLexiconTrainingDirectory(n).toString(), trainingFile);
    }

    public Path getConfigurationFile() {
        return Paths.get(getResourcesDirectory().toString(), "configuration.json");
    }
}
