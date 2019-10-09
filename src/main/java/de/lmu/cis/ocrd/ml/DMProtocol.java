package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DMProtocol implements Protocol {
    private static class ProtocolValue {
        String normalized = "";
        String ocr = "";
        String cor = "";
        double confidence = 0;
        boolean taken = false;
    }

    private static class Protocol {
        private final Map<String, ProtocolValue> corrections = new HashMap<>();
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
        final String word = token.getMasterOCR().getWordNormalized().toLowerCase();
        final ProtocolValue val = new ProtocolValue();
        val.normalized = token.getMasterOCR().getWordNormalized().toLowerCase();
        val.taken = taken;
        val.confidence = confidence;
        protocol.corrections.put(token.getMasterOCR().id(), val);
    }
}
