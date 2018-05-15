package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.FileTypes;
import de.lmu.cis.ocrd.ml.FeatureSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Environment {
    private static final String resources = "resources";
    private static final String dLex = "dLex";
    private static final String dLexFS = "features.ser";
    private static final String trainingFile = "training.arff";
    private static final String evaluationFile = "evaluation.arff";
    private final String path, name;
    private Path gt, masterOCR;
    private final List<Path> otherOCR = new ArrayList<>();
    private boolean copyTrainingFiles;
    private boolean debugTokenAlignment;

    public Environment(String base, String name) throws IOException {
        this.path = base;
        this.name = name;
        setupDirectories();
        setupTrainingDirectories(1);
    }

    private void setupDirectories() throws IOException {
        Files.createDirectory(getPath());
        Files.createDirectory(Paths.get(path, getResourcesDirectory().toString()));
        Files.createDirectory(Paths.get(path, getDynamicLexiconTrainingDirectory().toString()));
    }

    private void setupTrainingDirectories(int n) throws IOException {
        Files.createDirectory(Paths.get(path, getDynamicLexiconTrainingDirectory(n).toString()));
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return Paths.get(path, name);
    }

    public Path fullPath(Path path) {
        return Paths.get(this.path, path.toString());
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

    Document openGT() throws Exception {
        return FileTypes.openDocument(getOCRPath(getGT()).toString());
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

    Document openMasterOCR() throws Exception {
        return FileTypes.openDocument(getOCRPath(getMasterOCR()).toString());
    }

    private Path getOCRPath(Path ocr) {
        return copyTrainingFiles ? fullPath(ocr) : ocr;
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

    Document openOtherOCR(int i) throws Exception {
        return FileTypes.openDocument(getOCRPath(getOtherOCR(i)).toString());
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
        serializeFeatureSet(fs, fullPath(getDynamicLexiconFeatureSet()));
        return this;
    }

    public FeatureSet loadDynamicLexiconFeatureSet() throws IOException, ClassNotFoundException {
        return deSerializeFeatureSet(fullPath(getDynamicLexiconFeatureSet()));
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
        FileUtils.deleteDirectory(getPath().toFile());
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
        c.configuration = getConfigurationFile().toString();
        return c;
    }

    public Configuration loadConfiguration() throws IOException {
        return Configuration.fromJSON(fullPath(getConfigurationFile()));
    }

    public void writeConfiguration() throws IOException {
        try (PrintWriter out = new PrintWriter(fullPath(getConfigurationFile()).toFile())) {
            out.println(newConfiguration().toJSON());
        }
    }

    private Path copy(Path path) throws IOException {
        final Path target = Paths.get(fullPath(getResourcesDirectory()).toString(), path.getFileName().toString());
        if (Files.isDirectory(path)) {
            FileUtils.copyDirectory(path.toFile(), target.toFile());
        } else {
            FileUtils.copyFile(path.toFile(), target.toFile());
        }
        return Paths.get(getResourcesDirectory().toString(), path.getFileName().toString());
    }

    public Path getResourcesDirectory() {
        return Paths.get(name, resources);
    }

    public Path getDynamicLexiconTrainingDirectory() {
        return Paths.get(name, dLex);
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

    public void zipTo(Path zip) throws IOException {
        writeConfiguration();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
            eachFile((p)-> putZIPEntry(out, fullPath(p), p.toString()));
        }
    }

    private void putZIPEntry(ZipOutputStream out, Path path, String e) throws IOException {
        try {
            ZipEntry entry = new ZipEntry(e);
            out.putNextEntry(entry);
            IOUtils.copy(new FileInputStream(path.toFile()), out);
        } finally {
            out.closeEntry();
        }
    }

    public Path getDynamicLexiconEvaluationFile(int n) {
        return Paths.get(getDynamicLexiconTrainingDirectory(n).toString(), evaluationFile);
    }

    private interface EachFileCallback {
        void apply(Path path) throws IOException;
    }

    private void eachFile(EachFileCallback f) throws IOException {
        final Configuration configuration = newConfiguration();
        applyIfFileExists(f, Paths.get(configuration.configuration));
        applyIfFileExists(f, Paths.get(configuration.dynamicLexiconFeatureSet));
        for (int i = 0; i < configuration.dynamicLexiconTrainingFiles.length; i++) {
            applyIfFileExists(f, Paths.get(configuration.dynamicLexiconTrainingFiles[i]));
        }
        if (configuration.copyTrainingFiles) {
            applyIfFileExists(f, Paths.get(configuration.masterOCR));
            applyIfFileExists(f, Paths.get(configuration.gt));
            for (int i = 0; i < configuration.otherOCR.length; i++) {
                applyIfFileExists(f, Paths.get(configuration.otherOCR[i]));
            }
        }
    }

    private static void applyIfFileExists(EachFileCallback f, Path path) throws IOException {
        if (Files.exists(path)) {
            f.apply(path);
        }
    }
}
