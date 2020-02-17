package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class FeatureClassFilter {
    private final List<String> filterClasses;

    public FeatureClassFilter(List<String> classes) {
        this.filterClasses = classes;
    }

    boolean filter(JsonObject o) {
        if (filterClasses == null || !o.has("classes")) { // missing class or empty list; do not filter
            return false;
        }
        List<String> classes = getClasses(o);
        if (classes == null) {
            return false;
        }
        for (String filterClass : filterClasses) {
            for (String c: classes) {
                if (filterClass.equalsIgnoreCase(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getClasses(JsonObject o) {
        final JsonArray classes = o.get("classes").getAsJsonArray();//getAsString().toLowerCase();
        if (classes.isJsonNull()) {
            return null;
        }
        List<String> ret = new ArrayList<>(classes.size());
        for (JsonElement element: classes) {
            if (element.isJsonPrimitive()) {
                ret.add(element.getAsString());
            }
        }
        return ret;
    }
}
