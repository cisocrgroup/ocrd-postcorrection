package de.lmu.cis.ocrd.ml;

import java.util.ArrayList;
import java.util.Optional;

// TODO: Merge with Alignment.Token
public class Token {
    private final String masterOCR;
    private String gt;
    private ArrayList<String> otherOCR;

    public Token(String masterOCR) {
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

    public String getMasterOCR() {
        return this.masterOCR;
    }

    public Token addOCR(String ocr) {
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

    public String getOtherOCRAt(int i) {
        return otherOCR.get(i);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(masterOCR);
        char sep = '|';
        for (String other : otherOCR) {
            str.append(sep);
            str.append(other);
            sep = ',';
        }
        if (gt != null) {
            str.append("|GT:");
            str.append(gt);
        }
        return str.toString();
    }
}
