package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CandidateOCRToken implements OCRToken {
    private static final List<Ranking> EMPTY_RANKINGS = new ArrayList<>();
    private static final List<Candidate> EMPTY_CANDIDATES = new ArrayList<>();
    private final BaseOCRToken base;
    private final Candidate candidate;

    public CandidateOCRToken(BaseOCRToken base, Candidate candidate) {
        this.base = base;
        this.candidate = candidate;
    }

    @Override
    public List<Ranking> getRankings() {
        return EMPTY_RANKINGS;
    }

    @Override
    public Optional<Candidate> getCandidate() {
        return Optional.of(candidate);
    }

    @Override
    public List<Candidate> getCandidates() {
        return EMPTY_CANDIDATES;
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
