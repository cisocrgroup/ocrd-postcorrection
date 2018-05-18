package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;

import java.util.ArrayList;
import java.util.List;

public class TokenLengthFeature extends NamedStringSetFeature {
    private final static String SHORT = "short-token";
    private final static String MEDIUM = "medium-token";
    private final static String LONG = "long-token";
    private final static String VERY_LONG = "very-long-token";

    private static final List<String> CLASSES = new ArrayList<>();

    static {
        CLASSES.add(SHORT);
        CLASSES.add(MEDIUM);
        CLASSES.add(LONG);
        CLASSES.add(VERY_LONG);
    }

    private final int zhort, medium, lng;

    public TokenLengthFeature(JsonObject o, ArgumentFactory args) {
        this(JSONUtil.mustGet(o, "short").getAsInt(), JSONUtil.mustGet(o, "medium").getAsInt(),
                JSONUtil.mustGet(o, "long").getAsInt(), JSONUtil.mustGetNameOrType(o));
    }

    public TokenLengthFeature(int zhort, int medium, int lng, String name) {
        super(name, CLASSES);
        this.zhort = zhort;
        this.medium = medium;
        this.lng = lng;
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }

    @Override
    public Object calculate(Token token, int i, int n) {
        final int tokenLength = token.getMasterOCR().toString().length();
        if (tokenLength <= zhort) {
            return SHORT;
        }
        if (tokenLength <= medium) {
            return MEDIUM;
        }
        if (tokenLength <= lng) {
            return LONG;
        }
        return VERY_LONG;
    }
}
