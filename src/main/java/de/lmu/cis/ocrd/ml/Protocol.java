package de.lmu.cis.ocrd.ml;

import java.io.InputStream;
import java.io.OutputStream;

public interface Protocol {
    // Read the protocol from an input stream.
    void read(InputStream is) throws Exception;

    // Write the protocol into an output stream.
    void write(OutputStream out) throws Exception;

    // Protocol an OCRToken with its (optional correction), confidence
    // and whether the token was taken as correction/lexicon entry.
    void protocol(OCRToken token, String correction, double confidence, boolean taken);
}
