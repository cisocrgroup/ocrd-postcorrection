package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.ml.OCRWord;

import java.util.Optional;

public class BaseOCRToken implements de.lmu.cis.ocrd.ml.BaseOCRToken {
    private final int nOCR;

    BaseOCRToken(int nOCR) {
        this.nOCR = nOCR;
    }

    @Override
    public int getNOCR() {
        return nOCR;
    }

    @Override
    public OCRWord getMasterOCR() {
        return null;
    }

    @Override
    public OCRWord getSlaveOCR(int i) {
        return null;
    }

    @Override
    public Optional<String> getGT() {
        return Optional.empty();
    }

    @Override
    public void correct(String correction, double confidence) {

    }
}
