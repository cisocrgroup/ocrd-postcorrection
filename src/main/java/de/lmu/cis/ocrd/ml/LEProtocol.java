package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LEProtocol implements Protocol {
    private static class ProtocolValue {
        private double confidence = 0;
        private int count = 0;
    }
    private static class Protocol {
        private final Map<String, ProtocolValue> considered = new HashMap<>();
        private final Map<String, ProtocolValue> notConsidered = new HashMap<>();
    }
    private Protocol protocol = new Protocol();

    @Override
    public void read(InputStream is) {
        protocol = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), protocol.getClass());
    }

    @Override
    public void write(OutputStream out) throws Exception {
        out.write(new Gson().toJson(protocol).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void protocol(OCRToken token, double confidence, boolean taken) {
        // do *not* ignore case
        final String word = token.getMasterOCR().getWordNormalized();
        ProtocolValue val;
        if (taken) {
            val = protocol.considered.computeIfAbsent(word, k -> new ProtocolValue());
        } else {
            val = protocol.notConsidered.computeIfAbsent(word, k -> new ProtocolValue());
        }
        val.count++;
        val.confidence = confidence;
    }
}
