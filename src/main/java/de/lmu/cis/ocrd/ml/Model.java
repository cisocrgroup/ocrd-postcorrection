package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.FeatureClassFilter;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.FeatureSet;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

public class Model implements Closeable {
    private ZipFile zipFile;
    private Config config;

    public static Model open(Path path) throws Exception {
        Model m = new Model();
        m.zipFile = new ZipFile(path.toFile());
        m.config = m.readConfig();
        return m;
    }

    public InputStream openLEModel(int i) throws Exception {
        return openModel(config.leModels, i);
    }

    public InputStream openRRModel(int i) throws Exception {
        return openModel(config.rrModels, i);
    }

    public InputStream openDMModel(int i) throws Exception {
        return openModel(config.dmModels, i);
    }

    public InputStream openLanguageModel() throws Exception {
        return openFile(config.languageModelPath);
    }

    public long getCreated() {
        return config.created;
    }

    public List<JsonObject> getLEFeatureSet() {
        return config.leFeatureSet;
    }

    public List<JsonObject> getRRFeatureSet() {
        return config.rrFeatureSet;
    }

    public List<JsonObject> getDMFeatureSet() {
        return config.dmFeatureSet;
    }

    public FeatureSet getLEFeatures(LM lm) throws Exception {
        return FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(getLEFeatureSet(), new FeatureClassFilter(config.filterClasses));
    }

    public FeatureSet getRRFeatures(LM lm) throws Exception {
        return FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(getRRFeatureSet(), new FeatureClassFilter(config.filterClasses));
    }

    public FeatureSet getDMFeatures(LM lm) throws Exception {
        return FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(getDMFeatureSet(), new FeatureClassFilter(config.filterClasses));
    }

    public void addLEModel(Path path, int i) {
        addModel(initConfig().leModels, path, i);
    }

    public void addRRModel(Path path, int i) {
        addModel(initConfig().rrModels, path, i);
    }

    public void addDMModel(Path path, int i) {
        addModel(initConfig().dmModels, path, i);
    }

    public void setFilterClasses(List<String> filterClasses) {initConfig().filterClasses = filterClasses;}

    public List<String> getFilterClasses() {return config.filterClasses;}

    public void setLEFeatureSet(List<JsonObject> set) {
        initConfig().leFeatureSet = set;
    }

    public void setRRFeatureSet(List<JsonObject> set) {
        initConfig().rrFeatureSet = set;
    }

    public void setDMFeatureSet(List<JsonObject> set) {
        initConfig().dmFeatureSet = set;
    }

    public void setLanguageModelPath(String path) {
        initConfig().languageModelPath = path;
    }

    public void setCreated(long now) {
        initConfig().created = now;
    }

    public void save(Path path) throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        final URI uri = new URI("jar:" + path.toUri().getScheme(), path.toUri().getPath(), null);
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env, null)) {
            for (int i = 0; i < config.leModels.size(); i++) {
                config.leModels.set(i, copyInto(zipfs, config.leModels.get(i)));
            }
            for (int i = 0; i < config.rrModels.size(); i++) {
                config.rrModels.set(i, copyInto(zipfs, config.rrModels.get(i)));
            }
            for (int i = 0; i < config.dmModels.size(); i++) {
                config.dmModels.set(i, copyInto(zipfs, config.dmModels.get(i)));
            }
            config.languageModelPath = copyInto(zipfs, config.languageModelPath);
            writeConfig(zipfs);
        }
    }

    public void setDMTrainingType(String t) {
        initConfig().dmTrainingType = t;
    }

    public void setSeed(long seed) {
        initConfig().seed = seed;
    }

    public long getSeed() {
        return initConfig().seed;
    }

    public String getDMTrainingType() {return initConfig().dmTrainingType;}

    public void setNOCR(int nOCR) {
        initConfig().nOCR = nOCR;
    }

    public int getNOCR() {
        return config.nOCR;
    }

    public void setMaxCandidates(int maxCandidates) {
        initConfig().maxCandidates = maxCandidates;
    }

    public int getMaxCandidates() {
        return config.maxCandidates;
    }

    @Override
    public void close() throws IOException {
        if (zipFile != null) {
            zipFile.close();
        }
    }

    private void writeConfig(FileSystem fs) throws Exception {
        final String json = new Gson().toJson(config);
        Files.copy(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                fs.getPath("config.json"), StandardCopyOption.REPLACE_EXISTING);
    }

    private String copyInto(FileSystem fs, String path) throws Exception {
        final Path p = Paths.get(path);
        final String filename = p.getFileName().toString();
        final Path zip = fs.getPath(filename);
        Files.copy(p, zip, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    private void addModel(List<String> models, Path path, int i) {
        while (models.size() <= i) {
            models.add("");
        }
        models.set(i, path.toString());
    }

    private Config initConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    private Config readConfig() throws Exception {
        try (InputStream is = zipFile.getInputStream(zipFile.getEntry("config.json"))) {
            return new Gson().fromJson(new InputStreamReader(is), Config.class);
        }
    }

    private InputStream openModel(List<String> models, int i) throws Exception {
        return zipFile.getInputStream(zipFile.getEntry(models.get(i)));
    }

    private InputStream openFile(String path) throws Exception {
        if (path.endsWith(".gz")) { // check for gzipped file
            return new GZIPInputStream(zipFile.getInputStream(zipFile.getEntry(path)));
        }
        return zipFile.getInputStream(zipFile.getEntry(path));
    }

    private static class Config {
        List<String> leModels = new ArrayList<>();
        List<String> rrModels = new ArrayList<>();
        List<String> dmModels = new ArrayList<>();
        List<String> filterClasses = new ArrayList<>();
        List<JsonObject> leFeatureSet = new ArrayList<>();
        List<JsonObject> rrFeatureSet = new ArrayList<>();
        List<JsonObject> dmFeatureSet = new ArrayList<>();
        String languageModelPath = "";
        String dmTrainingType = "";
        long seed = 0;
        long created = 0;
        int nOCR = 0;
        int maxCandidates = 0;
    }
}
