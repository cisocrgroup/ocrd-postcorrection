package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.Token;

import java.util.Set;
import java.util.TreeSet;

public class TokenCaseFeature extends NamedStringSetFeature {
    private static final String LOWER = "all-lower-case";
    private static final String UPPER = "all-upper-case";
    private static final String TITLE = "title-case";
    private static final String MIXED = "mixed-case";
    private static final Set<String> CLASSES = new TreeSet<>();

    static {
        CLASSES.add(LOWER);
        CLASSES.add(UPPER);
        CLASSES.add(TITLE);
        CLASSES.add(MIXED);
    }

    public TokenCaseFeature(JsonObject o, ArgumentFactory args) {
        this(JSONUtil.mustGetNameOrType(o));
    }

    public TokenCaseFeature(String name) {
        super(name, CLASSES);
    }

    @Override
    public boolean handlesOCR(int i, int n) {
        return handlesOnlyMasterOCR(i, n);
    }

    @Override
    public Object calculate(Token token, int i, int n) {
        boolean allLowerCase = true;
        boolean allUpperCase = true;
        boolean firstUpperCase = false;
        boolean first = true;
        for (int c : getWord(token, i, n).toString().codePoints().toArray()) {
            final int type = Character.getType(c);
            if (type == Character.UPPERCASE_LETTER) {
                firstUpperCase = first;
                allLowerCase = false;
            } else if (type == Character.LOWERCASE_LETTER) {
                allUpperCase = false;
            }
            first = false;
        }
        if (allLowerCase) {
            return LOWER;
        }
        if (allUpperCase) {
            return UPPER;
        }
        if (firstUpperCase) {
            return TITLE;
        }
        return MIXED;
    }
}
