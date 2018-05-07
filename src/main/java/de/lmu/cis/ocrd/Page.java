package de.lmu.cis.ocrd;

import java.util.ArrayList;

public class Page implements Document {

    private final int pageSeq;
    private final ArrayList<OCRLine> lines;

    public Page(int pageSeq) {
        this.pageSeq = pageSeq;
        this.lines = new ArrayList<>();
    }

    public int getPageSeq() {
        return pageSeq;
    }

    @Override
    public void eachLine(Visitor v) throws Exception {
        for (OCRLine line : lines) {
            v.visit(line);
        }
    }

    public Page add(OCRLine line) {
        this.lines.add(line);
        return this;
    }
}
