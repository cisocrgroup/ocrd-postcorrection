package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class RankingsOCRToken implements OCRToken {
    private static final List<Candidate> EMPTY_CANDIDATES = new ArrayList<>();
    private final BaseOCRToken base;
    private final List<Ranking> rankings;

    public RankingsOCRToken(BaseOCRToken base, List<Ranking> rankings) {
        this.base = base;
        this.rankings = rankings;
    }

    public BaseOCRToken getBase() {
        return base;
    }

    @Override
    public List<Ranking> getRankings() {
        return rankings;
    }

    @Override
    public Optional<Candidate> getCandidate() {
        return Optional.empty();
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

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("|");
        sj.add(base.toString());
        for (Ranking ranking: getRankings()) {
            sj.add(ranking.candidate.toString() + ":" + ranking.ranking);
        }
        return sj.toString();
    }
}
