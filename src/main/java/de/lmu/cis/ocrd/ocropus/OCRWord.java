package de.lmu.cis.ocrd.ocropus;

public class OCRWord implements de.lmu.cis.ocrd.ml.OCRWord {
    @Override
    public String id() {
        return null;
    }

    @Override
    public String getWordNormalized() {
        return null;
    }

    @Override
    public String getLineNormalized() {
        return null;
    }

    @Override
    public String getWordRaw() {
        return null;
    }

    @Override
    public double getCharacterConfidenceAt(int i) {
        return 0;
    }

    @Override
    public double getConfidence() {
        return 0;
    }

    @Override
    public boolean isFirstInLine() {
        return false;
    }

    @Override
    public boolean isLastInLine() {
        return false;
    }
}
