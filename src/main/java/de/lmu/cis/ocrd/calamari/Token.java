package de.lmu.cis.ocrd.calamari;

import de.lmu.cis.ocrd.ml.BaseOCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.util.Normalizer;

import java.util.Optional;

public class Token implements BaseOCRToken {
    private final Word pOCR, sOCR;
    private final String gt;
    private final Data.Alternative[] alternatives;
    private final int id;

    Token(Data data, int id) {
        this.pOCR = new Word(data.PrimaryOCR, Normalizer.normalize(data.PrimaryLine), data.PrimaryConf, data.PrimaryCharConfs);
        this.sOCR = new Word(data.SecondaryOCR, Normalizer.normalize(data.SecondaryLine), data.SecondaryConf, null);
        this.gt = data.GT;
        this.alternatives = data.Alternatives;
        this.id = id;
    }
    @Override
    public int getNOCR() {
        return 2; // fixed number of OCR's for now.
    }

    @Override
    public String getID() {
        return Integer.toString(id);
    }

    @Override
    public OCRWord getMasterOCR() {
        return pOCR;
    }

    @Override
    public OCRWord getSlaveOCR(int i) {
        assert(i == 1);
        return sOCR;
    }

    @Override
    public Optional<String> getGT() {
        return Optional.of(gt);
    }

    @Override
    public void correct(String correction, double confidence, boolean take) {
        // do nothing
    }

    // Return the confidence for the matching alternative or -1 if the given
    // alternative does not exist.
    public double getAlternativeConf(String candidate) {
        if (this.alternatives == null) {
            return 0;
        }
        for (Data.Alternative alt: this.alternatives) {
            if (Normalizer.normalize(alt.Token.toLowerCase()).equals(candidate.toLowerCase())) {
                return alt.Conf;
            }
        }
        return 0;
    }
}
