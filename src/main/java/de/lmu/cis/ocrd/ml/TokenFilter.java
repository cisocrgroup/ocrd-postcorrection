package de.lmu.cis.ocrd.ml;

import java.util.List;
import java.util.stream.Stream;

public class TokenFilter {
    public interface Func {
        boolean apply(OCRToken token);
    }

    static Stream<OCRToken> filter(List<OCRToken> tokens) {
        return tokens.stream().filter((t)-> isNonLexicalToken(t) && isLongToken(t));
    }

    static Stream<OCRToken> filter(List<OCRToken> tokens, Func func) {
        return tokens.stream().filter((t)-> isNonLexicalToken(t) && isLongToken(t) && func.apply(t));
    }

    public static Stream<OCRToken> filter(OCRTokenReader tokenReader, Func func) throws Exception {
        return filter(tokenReader.read(), func);
    }

    public static Stream<OCRToken> filter(OCRTokenReader tokenReader) throws Exception {
        return filter(tokenReader.read());
    }

    private static boolean isNonLexicalToken(OCRToken token) {
        return !(token.getCandidates().size() == 1 && token.getCandidates().get(0).isLexiconEntry());
    }

    private static boolean isLongToken(OCRToken token) {
        return token.getMasterOCR().getWordNormalized().length() > 3;
    }
}
