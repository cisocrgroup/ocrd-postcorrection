package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;

public class FileGroupCandidateTokenReader extends FileGroupTokenReader {
    private List<OCRToken> candidateTokens;

    public FileGroupCandidateTokenReader(METS mets, String ifg) {
        super(mets, ifg);
    }

    @Override
    public List<OCRToken> readTokens() throws Exception {
        if (candidateTokens == null) {
            candidateTokens = readCandidateTokens();
        }
        return candidateTokens;
    }

    private List<OCRToken> readCandidateTokens() throws Exception {
        List<OCRToken> tokens = new ArrayList<>();
        for (OCRToken token : super.readTokens()) {
            for (Candidate candidate : token.getAllProfilerCandidates()) {
                tokens.add(new OCRTokenWithCandidateImpl(token, candidate));
            }
        }
        return tokens;
    }
}
