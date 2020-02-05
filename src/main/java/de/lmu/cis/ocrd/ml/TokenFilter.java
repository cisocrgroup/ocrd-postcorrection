package de.lmu.cis.ocrd.ml;

import java.util.List;
import java.util.stream.Stream;

public class TokenFilter {
    public interface Func {
        boolean apply(OCRToken token);
    }

    static Stream<OCRToken> filter(List<OCRToken> tokens) {
        return tokens.stream().filter((t)-> isNonLexical(t) && isLong(t));
    }

    static Stream<OCRToken> filter(List<OCRToken> tokens, Func func) {
        return tokens.stream().filter((t)-> isNonLexical(t) && isLong(t) && func.apply(t));
    }

    public static Stream<OCRToken> filter(OCRTokenReader tokenReader, Func func) throws Exception {
        return filter(tokenReader.read(), func);
    }

    public static Stream<OCRToken> filter(OCRTokenReader tokenReader) throws Exception {
        return filter(tokenReader.read());
    }

    static boolean isNonLexical(OCRToken token) {
        return !(token.getCandidates().size() == 1 && token.getCandidates().get(0).isLexiconEntry());
    }

    static boolean isLong(OCRToken token) {
        return token.getMasterOCR().getWordNormalized().length() > 3;
    }
}
