package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.ml.BaseOCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MockBaseOCRToken implements BaseOCRToken {
    private int nOCR;
    private List<MockOCRWord> words;

    public MockBaseOCRToken(int nOCR) {
        this.nOCR = nOCR;
        this.words = new ArrayList<>();
    }

    public MockBaseOCRToken addWord(MockOCRWord word) {
        words.add(word);
        return this;
    }

    @Override
    public String getID() {
        return "**MOCK**ID**";
    }

    @Override
    public int getNOCR() {
        return nOCR;
    }

    @Override
    public OCRWord getMasterOCR() {
        if (words.isEmpty()) {
            throw new RuntimeException("cannot access master ocr word: empty");
        }
        return words.get(0);
    }

    @Override
    public OCRWord getSlaveOCR(int i) {
        if ((i+1) >= words.size()) {
            throw new RuntimeException("cannot access master slave ocr: " + i);
        }
        return words.get(i+1);
    }

    @Override
    public Optional<String> getGT() {
        if (words.size() > nOCR) {
            return Optional.of(words.get(nOCR).getWordRaw());
        }
        return Optional.empty();
    }

    @Override
    public void correct(String correction, double confidence) {
        throw new RuntimeException("correct: not implemented");
    }
}
