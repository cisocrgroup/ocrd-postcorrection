package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.io.InputStream;
import java.io.OutputStream;

public interface Protocol {
    // Read the protocol from an input stream.
    void read(InputStream is) throws Exception;

    // Write the protocol into an output stream.
    void write(OutputStream out) throws Exception;

    // Protocol an OCRToken with its confidence and whether the token was taken as correction/lexicon entry.
    void protocol(OCRToken token, double confidence, boolean taken);
}
