package de.lmu.cis.ocrd.pagexml;

import java.util.Optional;

import de.lmu.cis.ocrd.profile.Candidate;

public class OCRTokenWithCandidateImpl extends OCRTokenImpl {
	private final Candidate candidate;

	public OCRTokenWithCandidateImpl(Word word, boolean withGT, Candidate c) {
		super(word, withGT);
		this.candidate = c;
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.of(candidate);
	}
}
