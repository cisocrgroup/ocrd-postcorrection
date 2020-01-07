package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileGroupTokenReader implements TokenReader {
    private final METS mets;
    private final String ifg;
    private  Profile profile;
    private List<Word> xmlWords;
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
    public List<OCRToken> readTokens(boolean candidateTokens) throws Exception {
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
            append(tokens, word, candidateTokens);
        }
        return tokens;
    }

    private void append(List<OCRToken> tokens, Word word, boolean candidateTokens) throws Exception {
        final List<TextEquiv> te = word.getTextEquivs();
        // skip token if gt is needed and the word does not have a text equiv with gt.
        if (gt && nOCR >= te.size()) {
            return;
        }

        final OCRToken token = new OCRTokenImpl(word, nOCR, maxCandidates, profile);
        if (candidateTokens) {
            Optional<Candidates> candidates = profile.get(token.getMasterOCR().toString().toLowerCase());
            if (!candidates.isPresent()) {
                return;
            }
            for (int i = 0; i < candidates.get().Candidates.size() && i < maxCandidates; i++) {
                tokens.add(new OCRTokenWithCandidateImpl(token, candidates.get().Candidates.get(i)));
            }
        } else {
            tokens.add(token);
        }
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
}
