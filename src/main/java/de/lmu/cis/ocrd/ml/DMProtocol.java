package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.util.StringCorrector;
import org.pmw.tinylog.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMProtocol implements Protocol {
    public static class Value {
        public GroundTruth gt;
        public String id = "";
        public String ocrNormalized = "";
        public String corNormalized = "";
        public String ocr = "";
        public String cor = "";
        public List<Ranking> rankings;
        public double confidence = 0;
        public boolean taken = false;
    }

    public static class GroundTruth {
        public String gt;
        public boolean present;

        public GroundTruth(OCRToken token) {
            present = token.getGT().isPresent();
            gt = token.getGT().orElse("");
        }
    }

    public static class Protocol {
        public final Map<String, Value> corrections = new HashMap<>();
    }

    private Protocol protocol = new Protocol();
    private Map<OCRToken, List<Ranking>> rankings;

    public DMProtocol() {}
    public DMProtocol(Map<OCRToken, List<Ranking>> rankings) {
        this.rankings = rankings;
    }

    public Protocol getProtocol() {
        return protocol;
    }

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
        Logger.debug("putting token into dm protocol: {} {} {} {}", token, correction, confidence, taken);
        if (protocol.corrections.containsKey(token.getID())) {
            throw new RuntimeException("not a unique id: " + token.getID() + " for token: " + token.toString());
        }
        if ("130".equals(token.getID())) {
            throw new RuntimeException("again this FUCKING token: " + token.toString());
        }
        final OCRWord masterOCR = token.getMasterOCR();
        final Value val = new Value();
        val.id = token.getID();
        val.gt = new GroundTruth(token);
        val.ocrNormalized = masterOCR.getWordNormalized().toLowerCase();
        val.corNormalized = correction.toLowerCase();
        val.ocr = masterOCR.getWordRaw();
        val.cor = new StringCorrector(val.ocr).correctWith(correction);
        val.confidence = confidence;
        val.taken = taken;
        val.rankings = rankings.get(token);
        protocol.corrections.put(token.getID(), val);
    }
}
