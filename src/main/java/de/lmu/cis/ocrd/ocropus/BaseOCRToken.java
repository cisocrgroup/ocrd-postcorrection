package de.lmu.cis.ocrd.ocropus;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class BaseOCRToken implements de.lmu.cis.ocrd.ml.BaseOCRToken {
    private final List<OCRWord> ocrs;
    private final String gt;
    private String id;

    BaseOCRToken(List<TSV> ocrs, List<String> normalizedLines, String gt) {
        this.ocrs = new ArrayList<>(ocrs.size());
        for (int i = 0; i < ocrs.size(); i++) {
            this.ocrs.add(new de.lmu.cis.ocrd.ocropus.OCRWord(ocrs.get(i), normalizedLines.get(i)));
        }
        this.gt = gt;
        this.id = "";
    }

    void setID(int id) {
        this.id = Integer.toString(id);
    }

    void setID(String id) {this.id = id;}

    @Override
    public String getID() {return id;}

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
    public void correct(String correction, double confidence, boolean take) {
        if (!take) {
            return;
        }
        Logger.debug("correcting {} with {}/{}", toString(), correction, confidence);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("id:" + getID());
        sj.add("mOCR:" + getMasterOCR().getWordNormalized());
        for (int i = 1; i < ocrs.size(); i++) {
            sj.add("OCR" + (i+1) + ":" + ocrs.get(i).getWordNormalized());
        }
        sj.add("GT:" + gt);
        return sj.toString();
    }
}
