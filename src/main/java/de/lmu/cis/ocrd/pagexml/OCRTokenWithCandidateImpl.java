package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.profile.Candidate;

import java.util.Optional;

public class OCRTokenWithCandidateImpl extends OCRTokenImpl {
	private final Candidate candidate;

	public OCRTokenWithCandidateImpl(Word word, int gtindex, Candidate c) {
		super(word, gtindex);
		this.candidate = c;
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.of(candidate);
	}
}
