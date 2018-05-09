package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// TODO: move this elsewhere
public class JSONUtil {
    public static JsonElement mustGet(JsonObject o, String key) {
        final JsonElement val = o.get(key);
        if (val == null) {
            throw new ClassCastException("JSON object does not have member " + key);
        }
        return val;
    }

    public static String mustGetNameOrType(JsonObject o) {
        JsonElement val = o.get("name");
        if (val != null) {
           return val.getAsString();
        }
        val = o.get("type");
        if (val != null) {
            return val.getAsString();
        }
        throw new ClassCastException("JSON object does not have name and/or type");
    }
}
