package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMProtocol implements Protocol {
    private static class Protocol {
        private final Map<String, List<String>> considered = new HashMap<>();
        private final Map<String, List<String>> notConsidered = new HashMap<>();
    }

    private final Protocol protocol = new Protocol();

    @Override
    public String toJSON() {
        return new Gson().toJson(protocol);
    }

    @Override
    public void register(OCRToken token, boolean considered) {
        final String word = token.getMasterOCR().getWord().toLowerCase();
        List<String> list;
        if (considered) {
            list = protocol.considered.computeIfAbsent(word, k -> new ArrayList<>());
        } else {
            list = protocol.notConsidered.computeIfAbsent(word, k -> new ArrayList<>());
        }
        list.add(token.getMasterOCR().id());
    }
}
