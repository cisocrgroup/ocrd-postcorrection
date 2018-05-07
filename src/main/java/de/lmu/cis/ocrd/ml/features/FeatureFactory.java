package de.lmu.cis.ocrd.ml.features;

import java.util.HashMap;

public class FeatureFactory {
    private final HashMap<String, String> features = new HashMap<>();

    // public void register(Feature .class)

    public void foo() {
        register(TokenLengthFeature.class);
    }

    private void register(Class<TokenLengthFeature> tokenLengthFeatureClass) {
        features.put(tokenLengthFeatureClass.getName(), "xx");
    }
}
