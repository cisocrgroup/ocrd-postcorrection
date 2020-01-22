package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class CandidateOCRToken extends AbstractOCRToken {
    private static final List<Ranking> EMPTY_RANKINGS = new ArrayList<>();
    private static final List<Candidate> EMPTY_CANDIDATES = new ArrayList<>();
    private final Candidate candidate;

    public CandidateOCRToken(BaseOCRToken base, Candidate candidate) {
        super(base);
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
    public String toString() {
        StringJoiner sj = new StringJoiner("|");
        sj.add(getBase().toString());
        sj.add(candidate.toString());
        return sj.toString();
    }
}
