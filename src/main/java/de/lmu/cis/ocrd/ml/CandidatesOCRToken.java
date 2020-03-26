package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class CandidatesOCRToken extends AbstractOCRToken {
    private static final List<Ranking> EMPTY_RANKINGS = new ArrayList<>();
    private static final List<Candidate> EMPTY_CANDIDATES = new ArrayList<>();
    private final List<Candidate> candidates;

    public CandidatesOCRToken(BaseOCRToken base) {
        this(base, null);
    }

    public CandidatesOCRToken(BaseOCRToken base, List<Candidate> candidates) {
        super(base);
        this.candidates = candidates;
    }

    @Override
    public List<Ranking> getRankings() {
        return EMPTY_RANKINGS;
    }

    @Override
    public Optional<Candidate> getCandidate() {
        return getCandidates().isEmpty() ?
                Optional.empty():
                Optional.of(getCandidates().get(0));
    }

    @Override
    public List<Candidate> getCandidates() {
        if (candidates == null) {
            return EMPTY_CANDIDATES;
        }
        return candidates;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("|");
        sj.add(getBase().toString());
        for (Candidate candidate: getCandidates()) {
            sj.add(candidate.toString());
        }
        return sj.toString();
    }
}
