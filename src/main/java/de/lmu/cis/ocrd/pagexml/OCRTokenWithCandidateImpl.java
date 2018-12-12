package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.profile.Candidate;

import java.util.Optional;

public class OCRTokenWithCandidateImpl extends OCRTokenImpl {
	private final Candidate candidate;

	public OCRTokenWithCandidateImpl(Word word, boolean withGT, Candidate c) {
		super(word, 0);
		this.candidate = c;
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.of(candidate);
	}
}
