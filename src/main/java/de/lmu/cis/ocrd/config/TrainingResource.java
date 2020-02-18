package de.lmu.cis.ocrd.config;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TrainingResource {
    final String dir, prefix;
    private final List<JsonObject> features;

    TrainingResource(String prefix, String dir, List<JsonObject> features) {
        this.prefix = prefix;
        this.dir = dir;
        this.features = features;
    }

    public Path getEvaluation(int n, boolean alex) {
        return Paths.get(dir, String.format("%s_eval%s_%d.json", prefix, alex ? "_alex":"", n));
    }


    public Path getModel(int n) {
        return Paths.get(dir, String.format("%s_model_%d.bin", prefix, n));
    }

    public Path getTraining(int n) {
        return Paths.get(dir, String.format("%s_training_%d.arff", prefix, n));
    }

    public List<JsonObject> getFeatures() {
        return features;
    }
}
