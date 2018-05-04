package de.lmu.cis.ocrd;

import java.util.Optional;

public class Word {
    private final SimpleLine line;
    private final int s, e;

    public Word(int s, int e, SimpleLine line) {
        this.s = s;
        this.e = e;
        this.line = line;
    }

    public static Word create(String str) {
        Optional<Word> word = SimpleLine.normalized(str, 0.0).getWord(str);
        assert(word.isPresent());
        return word.get();
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
