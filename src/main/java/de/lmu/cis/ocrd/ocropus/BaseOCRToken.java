package de.lmu.cis.ocrd.ocropus;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BaseOCRToken implements de.lmu.cis.ocrd.ml.BaseOCRToken {
    private final List<OCRWord> ocrs;
    private final String gt;

    BaseOCRToken(List<LLocs> ocrs, List<String> normalizedLines, String gt) {
        this.ocrs = new ArrayList<>(ocrs.size());
        for (int i = 0; i < ocrs.size(); i++) {
            this.ocrs.add(new de.lmu.cis.ocrd.ocropus.OCRWord(ocrs.get(i), normalizedLines.get(i)));
        }
        this.gt = gt;
    }

    @Override
    public int getNOCR() {
        return ocrs.size();
    }

    @Override
    public OCRWord getMasterOCR() {
        return ocrs.get(0);
    }

    @Override
    public OCRWord getSlaveOCR(int i) {
        return ocrs.get(i+1);
    }

    @Override
    public Optional<String> getGT() {
        return Optional.ofNullable(gt);
    }

    @Override
    public void correct(String correction, double confidence) {
        throw new NotImplementedException();
    }
}
