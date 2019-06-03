package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.util.HashMap;
import java.util.Map;

class DecisionMakerProtocol implements Protocol {
    private static class Protocol {
        private final Map<String, Integer> always = new HashMap<>();
        private final Map<String, Integer> sometimes = new HashMap<>();
        private final Map<String, Integer> never = new HashMap<>();
    }
    private static class Count {
        int considered = 0;
        int notConsidered = 0;
    }
    private final HashMap<String, Count> counts = new HashMap<>();

    @Override
    public String toJSON() {
        final Protocol protocol = new Protocol();
        counts.forEach((word, count) -> {
            if (count.considered > 0 && count.notConsidered > 0) {
                protocol.sometimes.put(word, count.considered + count.notConsidered);
            } else if (count.considered > 0) {
                protocol.always.put(word, count.considered);
            } else {
                protocol.never.put(word, count.notConsidered);
            }
        });
        return new Gson().toJson(protocol);
    }

    @Override
    public void register(OCRToken token, boolean considered) {
        final String word = token.getMasterOCR().getWord().toLowerCase();
        final Count count = counts.getOrDefault(word, new Count());
        if (considered) {
            count.considered += 1;
        } else {
            count.notConsidered += 1;
        }
        counts.put(word, count);
    }
}
