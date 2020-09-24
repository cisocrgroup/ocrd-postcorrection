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
    private final int maxCandidates;

    public CandidatesOCRToken(BaseOCRToken base) {
        this(base, 0, null);
    }

    public CandidatesOCRToken(BaseOCRToken base, int maxCandidates, List<Candidate> candidates) {
        super(base);
        this.maxCandidates = maxCandidates;
        if (maxCandidates == 0 || candidates == null) {
            this.candidates = EMPTY_CANDIDATES;
        } else {
            this.candidates = candidates;
        }
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
        assert(candidates != null);
        return candidates.subList(0, Math.min(candidates.size(), maxCandidates));
    }

    @Override
    public List<Candidate> getAllCandidates() {
        assert(candidates != null);
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
