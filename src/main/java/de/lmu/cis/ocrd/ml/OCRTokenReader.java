package de.lmu.cis.ocrd.ml;

import java.util.List;

public interface OCRTokenReader {
    List<OCRToken> read() throws Exception;
}
