package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.util.Normalizer;

import java.util.StringJoiner;

public class OCRWord implements de.lmu.cis.ocrd.ml.OCRWord {
    private final TSV word;
    private final String normalizedLine;
    private final String normalized;
    private final double averageConfidence;

    OCRWord(TSV word, String normalizedLine) {
        this.word = word;
        this.normalizedLine = normalizedLine;
        this.normalized = Normalizer.normalize(word.toString());
        this.averageConfidence = word.getAverageConfidence();
    }

    @Override
    public String getWordNormalized() {
        return normalized;
    }

    @Override
    public String getLineNormalized() {
        return normalizedLine;
    }

    @Override
    public String getWordRaw() {
        return word.toString();
    }

    @Override
    public double getCharacterConfidenceAt(int i) {
        return word.at(i).getConfidence();
    }

    @Override
    public double getConfidence() {
        return averageConfidence;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < word.length(); i++) {
            sj.add(Double.toString(word.at(i).getConfidence()));
        }
        return word.toString() + "|" + sj.toString();
    }
}
