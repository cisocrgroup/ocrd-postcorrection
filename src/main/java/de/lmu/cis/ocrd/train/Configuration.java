package de.lmu.cis.ocrd.train;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

// Data class for the configuration of the training environment.
public class Configuration {
    public String gt, masterOCR, dynamicLexiconFeatureSet, configuration;
    public String[] otherOCR;
    public String[] dynamicLexiconTrainingFiles;
    public String[] dynamicLexiconEvaluationFiles;
    String[] dynamicLexiconModelFiles;
    public boolean copyTrainingFiles, debugTokenAlignment;

    static Configuration fromJSON(Path path) throws IOException {
        try (InputStream in = new FileInputStream(path.toFile())) {
            return fromJSON(in);
        }
    }

    private static Configuration fromJSON(InputStream in) throws IOException {
        final String json = IOUtils.toString(in, Charsets.UTF_8);
        return new Gson().fromJson(json, Configuration.class);
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
