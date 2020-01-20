package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileGroupTokenReader implements TokenReader, WordReader {
    private final METS mets;
    private final String ifg;
    private final Parameters parameters;
    private  Profile profile;
    private List<Word> xmlWords;
    private List<OCRToken> tokens;

    public FileGroupTokenReader(METS mets, Parameters parameters, String ifg) {
        this.mets = mets;
        this.parameters = parameters;
        this.ifg = ifg;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        // update profile in tokens
        if (tokens != null) {
            for (OCRToken token : tokens) {
                ((OCRTokenImpl) token).setProfile(profile);
            }
        }
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
            tokens.add(new OCRTokenImpl(word, parameters.getNOCR(), parameters.getMaxCandidates(), profile));
        }
        return tokens;
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
                    for (Word word : line.getWords()) {
                        words.add(word);
                    }
                }
            }
        }
        return words;
    }
}
