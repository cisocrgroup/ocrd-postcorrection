package de.lmu.cis.ocrd.config;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LexiconTrainingResource extends ProtocolTrainingResource {
    LexiconTrainingResource(String prefix, String dir, List<JsonObject> features) {
        super(prefix, dir, features);
    }

    public Path getLexicon(int n) {
        return Paths.get(dir, String.format("le_%d.txt", n));
    }
}
