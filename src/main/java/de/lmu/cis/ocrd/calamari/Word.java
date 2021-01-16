package de.lmu.cis.ocrd.calamari;

import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.util.Normalizer;
import org.pmw.tinylog.Logger;

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
        Logger.debug("{{calamari.Word}}");
        Logger.debug("raw = {}", this.raw);
        Logger.debug("normalized = {}", this.normalized);
        Logger.debug("line = {}", this.line);
        Logger.debug("conf = {}", this.conf);
        Logger.debug("null = {}", this.charConfs == null);
        if (this.charConfs != null) {
            Logger.debug("len = {}", this.charConfs.length);
        }
        return charConfs[i];
    }

    @Override
    public double getConfidence() {
        return conf;
    }
}
