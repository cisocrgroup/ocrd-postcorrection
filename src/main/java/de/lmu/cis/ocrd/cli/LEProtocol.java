package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.util.HashMap;
import java.util.Map;

class LEProtocol implements Protocol {
    private static class Protocol {
        private final Map<String, Integer> considered = new HashMap<>();
        private final Map<String, Integer> notConsidered = new HashMap<>();
    }
    private final Protocol protocol = new Protocol();

    @Override
    public String toJSON() {
        return new Gson().toJson(protocol);
    }

    @Override
    public void register(OCRToken token, boolean considered) {
        // do *not* ignore case
        final String word = token.getMasterOCR().getWord();
        if (considered) {
            final int count = protocol.considered.getOrDefault(word, 0);
            protocol.considered.put(word, count + 1);
        } else if (!token.isLexiconEntry()) { // not considered
            final int count = protocol.notConsidered.getOrDefault(word, 0);
            protocol.notConsidered.put(word, count + 1);
        }
    }
}
