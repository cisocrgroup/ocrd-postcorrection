package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.OCRWord;
import de.lmu.cis.ocrd.util.StringCorrector;

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
    public void protocol(OCRToken token, String correction, double confidence, boolean taken) {
        final OCRWord word = token.getMasterOCR();
        final ProtocolValue val = new ProtocolValue();
        val.normalized = word.getWordNormalized().toLowerCase();
        val.ocr = word.getWordRaw();
        val.cor = new StringCorrector(val.ocr).correctWith(correction);
        val.confidence = confidence;
        val.taken = taken;
        protocol.corrections.put(token.getMasterOCR().id(), val);
    }
}
