package de.lmu.cis.ocrd.config;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProtocolTrainingResource extends TrainingResource {
    ProtocolTrainingResource(String prefix, String dir, List<JsonObject> features) {
        super(prefix, dir, features);
    }

    public Path getProtocol(int n, boolean alex) {
        return Paths.get(dir, String.format("%s_protocol%s_%d.json", prefix, alex? "_alex": "", n));
    }
}
