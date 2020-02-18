package de.lmu.cis.ocrd.ml;

import java.util.List;

public interface BaseOCRTokenReader {
    List<BaseOCRToken> read() throws Exception;
}
