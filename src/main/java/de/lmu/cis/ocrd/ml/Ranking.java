package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.profile.Candidate;

public class Ranking {
    private final Candidate candidate;
    private final double ranking;

    public Ranking(Candidate candidate, double ranking) {
       this.candidate = candidate;
       this.ranking = ranking;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public double getRanking() {
        return ranking;
    }
}
