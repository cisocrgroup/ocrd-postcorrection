package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class METSFileGroupWordReader implements WordReader {
    private final METS mets;
    private final String ifg;
    private final Parameters parameters;
    private  Profile profile;
    private List<Word> xmlWords;
    private List<OCRToken> tokens;

    METSFileGroupWordReader(METS mets, Parameters parameters, String ifg) {
        this.mets = mets;
        this.parameters = parameters;
        this.ifg = ifg;
    }

    @Override
    public List<Word> readWords() throws Exception {
        if (xmlWords == null) {
            xmlWords = doReadWords();
        }
        return xmlWords;
    }

    private List<Word> doReadWords() throws Exception {
        ArrayList<Word> words = new ArrayList<>();
        for (METS.File file : mets.findFileGrpFiles(ifg)) {
            try (InputStream is = file.openInputStream()) {
                final Page page = Page.parse(Paths.get(file.getFLocat()), is);
                for (Line line : page.getLines()) {
                    words.addAll(line.getWords());
                }
            }
        }
        return words;
    }
}
