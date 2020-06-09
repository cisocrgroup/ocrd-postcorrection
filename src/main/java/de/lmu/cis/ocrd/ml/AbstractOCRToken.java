package de.lmu.cis.ocrd.ml;

import java.util.Optional;

public abstract class AbstractOCRToken implements OCRToken {
    private final BaseOCRToken base;

    AbstractOCRToken(BaseOCRToken base) {
        if (base instanceof AbstractOCRToken) {
            this.base = ((AbstractOCRToken) base).getBase();
        } else {
            this.base = base;
        }
    }

    public BaseOCRToken getBase() {
        return base;
    }

    @Override
    public String getID() {return base.getID();}

    @Override
    public int getNOCR() {
        return base.getNOCR();
    }

    @Override
    public OCRWord getMasterOCR() {
        return base.getMasterOCR();
    }

    @Override
    public OCRWord getSlaveOCR(int i) {
        return base.getSlaveOCR(i);
    }

    @Override
    public Optional<String> getGT() {
        return base.getGT();
    }

    @Override
    public void correct(String correction, double confidence, boolean take) {
        base.correct(correction, confidence, take);
    }
}
