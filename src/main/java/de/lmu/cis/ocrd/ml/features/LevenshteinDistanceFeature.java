package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class LevenshteinDistanceFeature extends NamedDoubleFeature {
    private final LevenshteinDistance ld;

    @SuppressWarnings("WeakerAccess")
    LevenshteinDistanceFeature(String name, int threshold) {
        super(name);
        ld = new LevenshteinDistance(threshold);
    }

    public LevenshteinDistanceFeature(JsonObject o, ArgumentFactory ignore) {
        this(JSON.mustGetNameOrType(o), JSON.mustGet(o, "maxThreshold").getAsInt());
    }

    public int getThreshold() {
        final Integer threshold = ld.getThreshold();
        return threshold == null? -1 : threshold;
    }

    @Override
    protected double doCalculate(OCRToken token, int i, int n) {
        assert(handlesOCR(i, n));
        final String master = token.getMasterOCR().getWordNormalized();
        final String slave = getWord(token, i, n).getWordNormalized();
        final int distance = ld.apply(master, slave);
        return distance == -1? getThreshold(): distance;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesEverySlaveOCR(i, n);
    }
}
