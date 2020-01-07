package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.util.List;

public interface TokenReader {
    List<OCRToken> readTokens() throws Exception;
}
