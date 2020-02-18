package de.lmu.cis.ocrd.calamari;

import com.google.gson.annotations.SerializedName;

public class Char {
    @SerializedName("char") private String character;
    private double probability;
    private int label;

    public String getChar() {
        return character;
    }

    public double getProbability() {
        return probability;
    }

    public int getLabel() {
        return label;
    }
}
