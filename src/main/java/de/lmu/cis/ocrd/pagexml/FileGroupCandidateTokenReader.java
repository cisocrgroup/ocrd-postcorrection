package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;

public class FileGroupCandidateTokenReader implements TokenReader {
    private final TokenReader tokenReader;
    private List<OCRToken> tokens;

    public FileGroupCandidateTokenReader(TokenReader tokenReader) {
        this.tokenReader = tokenReader;
    }

    @Override
    public List<OCRToken> readTokens() throws Exception {
        if (tokens == null) {
            tokens = readCandidateTokens();
        }
        return tokens;
    }

    private List<OCRToken> readCandidateTokens() throws Exception {
        List<OCRToken> tokens = new ArrayList<>();
        for (OCRToken token : tokenReader.readTokens()) {
            for (Candidate candidate : token.getAllProfilerCandidates()) {
                tokens.add(new OCRTokenWithCandidateImpl(token, candidate));
            }
        }
        return tokens;
    }
}
