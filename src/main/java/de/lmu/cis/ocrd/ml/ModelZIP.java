package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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

public class ModelZIP implements Closeable {
    private ZipFile zipFile;
    private Config config;

    public static ModelZIP open(Path path) throws Exception {
        ModelZIP m = new ModelZIP();
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

    public void addLEModel(Path path, int i) {
        addModel(initConfig().leModels, path, i);
    }

    public void addRRModel(Path path, int i) {
        addModel(initConfig().rrModels, path, i);
    }

    public void addDMModel(Path path, int i) {
        addModel(initConfig().dmModels, path, i);
    }

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
        URI uri = new URI("jar:" + path.toUri().getScheme(), path.toUri().getPath(), null);
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
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
        List<JsonObject> leFeatureSet = new ArrayList<>();
        List<JsonObject> rrFeatureSet = new ArrayList<>();
        List<JsonObject> dmFeatureSet = new ArrayList<>();
        String languageModelPath = "";
        long created = 0;
    }
}
