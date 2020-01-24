package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.util.Normalizer;

public class OCRWord implements de.lmu.cis.ocrd.ml.OCRWord {
    private final LLocs word;
    private final String normalizedLine;
    private final String normalized;
    private final String id;
    private final double averageConfidence;

    OCRWord(LLocs word, String normalizedLine) {
        this.word = word;
        this.normalizedLine = normalizedLine;
        this.normalized = Normalizer.normalize(word.toString());
        this.averageConfidence = word.getAverageConfidence();
        this.id = getID(word);
    }

    @Override
    public String id() {
        return id;
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
    public boolean isFirstInLine() {
        return normalizedLine.startsWith(normalized);
    }

    @Override
    public boolean isLastInLine() {
        return normalizedLine.endsWith(normalized);
    }

    private static String getID(LLocs llocs) {
        final int pos = llocs.getPath().getFileName().toString().indexOf('.');
        return llocs.getPath().getParent().getFileName().toString() + ":" + llocs.getPath().getFileName().toString().substring(pos+1);
    }
}
