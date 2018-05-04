package de.lmu.cis.ocrd;

public class Word {
    private final SimpleLine line;
    private final int s, e;

    public Word(int s, int e, SimpleLine line) {
        this.s = s;
        this.e = e;
        this.line = line;
    }

    public SimpleLine getLine() {
        return line;
    }

    public double getConfidenceAt(int i) {
        return line.getConfidenceAt(s + i);
    }

    @Override
    public String toString() {
        return line.getNormalized().substring(s, e);
    }
}
