package de.lmu.cis.ocrd.ml;

import java.util.List;

public interface TokenReader {
    List<OCRToken> readTokens() throws Exception;
}
