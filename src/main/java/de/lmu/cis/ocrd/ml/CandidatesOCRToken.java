package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CandidatesOCRToken implements OCRToken {
    private static final List<Ranking> EMPTY_RANKINGS = new ArrayList<>();
    private final BaseOCRToken base;
    private final List<Candidate> candidates;

    public CandidatesOCRToken(BaseOCRToken base, List<Candidate> candidates) {
        this.base = base;
        this.candidates = candidates;
    }

    @Override
    public List<Ranking> getRankings() {
        return EMPTY_RANKINGS;
    }

    @Override
    public Optional<Candidate> getProfilerCandidate() {
        return Optional.empty();
    }

    @Override
    public List<Candidate> getAllProfilerCandidates() {
        return candidates;
    }

    @Override
    public int getNOCR() {
        return base.getNOCR();
    }

    @Override
    public OCRWord getMasterOCR() {
        return base.getMasterOCR();
    }

    @Override
    public OCRWord getSlaveOCR(int i) {
        return base.getSlaveOCR(i);
    }

    @Override
    public Optional<String> getGT() {
        return base.getGT();
    }

    @Override
    public void correct(String correction, double confidence) {
        base.correct(correction, confidence);
    }
}
