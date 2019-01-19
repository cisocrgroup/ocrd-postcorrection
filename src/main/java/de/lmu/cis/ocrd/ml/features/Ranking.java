package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.profile.Candidate;

public class Ranking {
    public final Candidate candidate;
    public final double ranking;
    public Ranking(Candidate candidate, double ranking) {
       this.candidate = candidate;
       this.ranking = ranking;
    }
}
