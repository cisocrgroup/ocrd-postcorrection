package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.Word;

import java.util.ArrayList;
import java.util.Optional;

// TODO: Merge with Alignment.Token
public class Token {
    private final Word masterOCR;
    private String gt;
    private ArrayList<Word> otherOCR;

    public static Token create(String str) {
        return new Token(Word.create(str));
    }
    public Token(Word masterOCR) {
        assert masterOCR != null;
        this.masterOCR = masterOCR;
    }

    public Token withGT(String gt) {
        this.gt = gt;
        return this;
    }

    public Optional<String> getGT() {
        return Optional.ofNullable(gt);
    }

    public Word getMasterOCR() {
        return this.masterOCR;
    }

    public Token addOCR(Word ocr) {
        assert ocr != null;
        if (this.otherOCR == null) {
            this.otherOCR = new ArrayList<>();
        }
        this.otherOCR.add(ocr);
        return this;
    }

    public int getNumberOfOtherOCRs() {
        if (otherOCR == null) {
            return 0;
        }
        return otherOCR.size();
    }

    public Word getOtherOCRAt(int i) {
        return otherOCR.get(i);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(masterOCR.toString());
        char sep = '|';
        for (Word other : otherOCR) {
            str.append(sep);
            str.append(other.toString());
            sep = ',';
        }
        if (gt != null) {
            str.append("|GT:");
            str.append(gt);
        }
        return str.toString();
    }
}
