package de.lmu.cis.ocrd.calamari;

import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.util.Normalizer;

class Word implements OCRWord {
    private final String normalized;
    private final String raw;
    private final String line;
    private final double[] charConfs;
    private final double conf;

    Word(String raw, String line, double conf, double[] charConfs) {
        this.raw = raw;
        this.normalized = Normalizer.normalize(raw);
        this.line = line;
        this.conf = conf;
        this.charConfs = charConfs;
    }

    @Override
    public String getWordNormalized() {
        return normalized;
    }

    @Override
    public String getLineNormalized() {
        return line;
    }

    @Override
    public String getWordRaw() {
        return raw;
    }

    @Override
    public double getCharacterConfidenceAt(int i) {
        if (this.charConfs == null || this.charConfs.length <= i) {
            return 0;
        }
        return charConfs[i];
    }

    @Override
    public double getConfidence() {
        return conf;
    }
}
