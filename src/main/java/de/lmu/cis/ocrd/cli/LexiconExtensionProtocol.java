package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LexiconExtensionProtocol implements Protocol {
    private static class Protocol {
        private final Map<String, Integer> consideredWordFrequencies = new HashMap<>();
        private final List<String> notLexicalNotConsidered = new ArrayList<>();
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
            final int count = protocol.consideredWordFrequencies.getOrDefault(word, 0);
            protocol.consideredWordFrequencies.put(word, count + 1);
            return;
        }
        // not considered
        if (!token.isLexiconEntry()) {
            protocol.notLexicalNotConsidered.add(word);
        }
    }
}
