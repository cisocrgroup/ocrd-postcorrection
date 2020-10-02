package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.util.StringCorrector;
import org.pmw.tinylog.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMProtocol implements Protocol {
    public static class BaseValue {
        public GroundTruth gt;
        public String id = "";
        String ocrNormalized = "";
        String corNormalized = "";
        public String ocr = "";
        String cor = "";
        public double confidence = 0;
        public boolean taken = false;

        public boolean correctionIsCorrect() {
            return corNormalized.toLowerCase().equals(gt.gt.toLowerCase());
        }

        public boolean ocrIsCorrect() {
            return ocrNormalized.toLowerCase().equals(gt.gt.toLowerCase());
        }
    }

    public static class Value extends BaseValue {
        public List<Ranking> rankings;

        // -1: no good correction candidate
        // 0: good correction candidate
        public int getCorrectCandidateIndex() {
            if (rankings == null) {
                return -1;
            }
            for (int i = 0; i< rankings.size(); i++) {
                if (rankings.get(i).getCandidate().Suggestion.toLowerCase().equals(gt.gt.toLowerCase())) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static class TValue extends BaseValue {
        public List<Candidate> candidates;
    }

    public static class Candidate {
        int histDistance, ocrDistance;
        String suggestion;
        public Candidate(de.lmu.cis.ocrd.profile.Candidate candidate) {
            suggestion = candidate.Suggestion;
            ocrDistance = candidate.Distance;
            if (candidate.HistPatterns == null) {
                histDistance = 0;
            } else {
                histDistance = candidate.HistPatterns.length;
            }

        }
    }

    public static class GroundTruth {
        public String gt;
        public boolean present;

        GroundTruth(OCRToken token) {
            present = token.getGT().isPresent();
            gt = token.getGT().orElse("");
        }
    }

    public static class Protocol {
        public final Map<String, Value> corrections = new HashMap<>();
        public final Map<String, TValue> tokens = new HashMap<>();
    }

    private Protocol protocol = new Protocol();
    private Map<OCRToken, List<Ranking>> rankings;

    public DMProtocol() {}

    public void setRankings(Map<OCRToken, List<Ranking>> rankings) {
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
    public void protocol(OCRToken token, String correction, double confidence, boolean taken) throws Exception {
        Logger.debug("putting token into dm protocol: {} {} {} {}", token, correction, confidence, taken);
        if (protocol.corrections.containsKey(token.getID())) {
            throw new Exception("not a unique id: " + token.getID() + " for token: " + token.toString());
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

        // Update the according token in the token map
        if (!protocol.tokens.containsKey(token.getID())) {
            throw new Exception("cannot find token with id: " + token.getID());
        }
        TValue v = protocol.tokens.get(token.getID());
        v.corNormalized = val.corNormalized;
        v.cor = val.cor;
        v.confidence = val.confidence;
        v.taken = val.taken;
    }

    public void protocol(OCRToken token) throws Exception {
        if (protocol.tokens.containsKey(token.getID())) {
            throw new Exception("not a unique id: " + token.getID() + " for token: " + token.toString());
        }
        final OCRWord mOCR = token.getMasterOCR();
        final TValue v = new TValue();
        v.id = token.getID();
        v.gt = new GroundTruth(token);
        v.ocrNormalized = mOCR.getWordNormalized();
        v.corNormalized = ""; // gets updated in the other protocol function
        v.ocr = mOCR.getWordRaw();
        v.cor = ""; // updated later
        v.confidence = 0; // updated later
        v.taken = false; // updated later
        v.candidates = new ArrayList<>();
        for (de.lmu.cis.ocrd.profile.Candidate candidate: token.getAllCandidates()) {
            v.candidates.add(new Candidate(candidate));
        }
        protocol.tokens.put(token.getID(), v);
    }
}
