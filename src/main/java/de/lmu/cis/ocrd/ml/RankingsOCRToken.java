package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class RankingsOCRToken extends AbstractOCRToken {
    private static final List<Candidate> EMPTY_CANDIDATES = new ArrayList<>();
    private final List<Ranking> rankings;

    public RankingsOCRToken(BaseOCRToken base, List<Ranking> rankings) {
        super(base);
        this.rankings = rankings;
    }

    @Override
    public List<Ranking> getRankings() {
        return rankings;
    }

    @Override
    public Optional<Candidate> getCandidate() {
        return Optional.of(rankings.get(0).getCandidate());//Optional.empty();
    }

    @Override
    public List<Candidate> getCandidates() {
        return EMPTY_CANDIDATES;
    }

    @Override
    public List<Candidate> getAllCandidates() {return EMPTY_CANDIDATES;}

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("|");
        sj.add(getBase().toString());
        for (Ranking ranking: getRankings()) {
            sj.add(ranking.getCandidate().toString() + ":" + ranking.getRanking());
        }
        return sj.toString();
    }
}
