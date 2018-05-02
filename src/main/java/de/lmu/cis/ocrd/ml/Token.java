package de.lmu.cis.ocrd.ml;

import java.util.ArrayList;
import java.util.Optional;

public class Token {
    private final String masterOCR;
    private Optional<String> gt;
    private ArrayList<String> otherOCR;

    public Token(String masterOCR) {
        assert masterOCR != null;
        this.masterOCR = masterOCR;
        this.gt = Optional.empty();
    }

    public Token withGT(String gt) {
        this.gt = Optional.ofNullable(gt);
        return this;
    }

    public Optional<String> getGT() {
        return this.gt;
    }

    public String getMasterOCR() {
        return this.masterOCR;
    }

    public Token addOCR(String ocr) {
        assert ocr != null;
        if (this.otherOCR == null) {
            this.otherOCR = new ArrayList<String>();
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
}
