package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.ml.OCRWord;

public class MockOCRWord implements OCRWord {
    private String id = "";
    private String wordNormalized = "";
    private String lineNormalized = "";
    private String wordRaw = "";
    private double confidence = 0.0;
    private double[] charConfidences = new double[0];


    public MockOCRWord setId(String id) {
        this.id = id;
        return this;
    }

    public MockOCRWord setWordNormalized(String wordNormalized) {
        this.wordNormalized = wordNormalized;
        return this;
    }

    public MockOCRWord setLineNormalized(String lineNormalized) {
        this.lineNormalized = lineNormalized;
        return this;
    }

    public MockOCRWord setWordRaw(String wordRaw) {
        this.wordRaw = wordRaw;
        return this;
    }

    public MockOCRWord setConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    public MockOCRWord setCharConfidences(double[] charConfidences) {
        this.charConfidences = charConfidences;
        return this;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getWordNormalized() {
        return wordNormalized;
    }

    @Override
    public String getLineNormalized() {
        return lineNormalized;
    }

    @Override
    public String getWordRaw() {
        return wordRaw;
    }

    @Override
    public double getCharacterConfidenceAt(int i) {
        if (i < charConfidences.length) {
            return charConfidences[i];
        }
        return 0;
    }

    @Override
    public double getConfidence() {
        return confidence;
    }
}
