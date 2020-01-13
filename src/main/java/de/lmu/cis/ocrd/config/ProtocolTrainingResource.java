package de.lmu.cis.ocrd.config;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProtocolTrainingResource extends TrainingResource {
    ProtocolTrainingResource(String prefix, String dir, List<JsonObject> features) {
        super(prefix, dir, features);
    }

    Path getProtocol(int n) {
        return Paths.get(dir, String.format("%s_protocol_%d.txt", prefix, n));
    }
}
