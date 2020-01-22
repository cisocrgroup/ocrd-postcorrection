package de.lmu.cis.ocrd.ml;

import java.util.List;
import java.util.stream.Stream;

class TokenFilter {
    static Stream<OCRToken> filter(List<OCRToken> tokens) {
        return tokens.stream().filter((t)-> !tokenIsLexiconEntry(t) && !tokenIsTooShort(t));
    }

    private static boolean tokenIsLexiconEntry(OCRToken token) {
        return token.getCandidates().size() == 1 && token.getCandidates().get(0).isLexiconEntry();
    }

    private static boolean tokenIsTooShort(OCRToken token) {
        return token.getMasterOCR().getWordNormalized().length() <= 3;
    }
}
