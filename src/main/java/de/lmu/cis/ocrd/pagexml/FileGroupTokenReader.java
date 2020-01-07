package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.TokenReaderFactory;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileGroupTokenReader implements TokenReader {
    private final METS mets;
    private final String ifg;
    private  Profile profile;
    private List<Word> xmlWords;
    private List<OCRToken> tokens;
    private int nOCR;
    private int maxCandidates;
    private boolean gt;

    public FileGroupTokenReader(METS mets, String ifg) {
        this.mets = mets;
        this.ifg = ifg;
    }

    public FileGroupTokenReader withNOCR(int nOCR) {
        this.nOCR = nOCR;
        return this;
    }

    public FileGroupTokenReader withMaxCandidates(int maxCandidates) {
        this.maxCandidates = maxCandidates;
        return this;
    }

    public FileGroupTokenReader withProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    public FileGroupTokenReader withGT(boolean gt) {
        this.gt = gt;
        return this;
    }

    @Override
    public List<OCRToken> readTokens() throws Exception {
        if (tokens == null) {
            tokens = doReadTokens();
        }
        return tokens;
    }

    private List<OCRToken> doReadTokens() throws Exception {
        ArrayList<OCRToken> tokens = new ArrayList<>();
        for (Word word : readWords()) {
            final List<String> unicodeNormalized = word.getUnicodeNormalized();
            if (unicodeNormalized.isEmpty()) {
                continue;
            }
            final String mOCR = unicodeNormalized.get(0);
            if (mOCR.length() <= 3) {
                continue;
            }
            final List<TextEquiv> te = word.getTextEquivs();
            // skip token if gt is needed and the word does not have a text equiv with gt.
            if (gt && nOCR >= te.size()) {
                continue;
            }
            tokens.add(new OCRTokenImpl(word, nOCR, maxCandidates, profile));
        }
        return tokens;
    }

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
                    for (Word word : line.getWords()) {
                        words.add(word);
                    }
                }
            }
        }
        return words;
    }

    public static class Factory implements TokenReaderFactory {
        private final METS mets;

        public Factory(METS mets) {
            this.mets = mets;
        }

        @Override
        public TokenReader create(String ifg) {
            return new FileGroupCandidateTokenReader(mets, ifg);
        }
    }

}
