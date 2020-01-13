package de.lmu.cis.ocrd.pagexml;

import java.util.List;

public interface WordReader {
    List<Word> readWords() throws Exception;
}
