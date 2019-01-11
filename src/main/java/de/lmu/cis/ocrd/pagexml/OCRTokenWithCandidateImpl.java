package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.OCRWord;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.List;
import java.util.Optional;

public class OCRTokenWithCandidateImpl implements OCRToken {
	private final Candidate candidate;
	private final OCRToken token;

	public OCRTokenWithCandidateImpl(OCRToken token, Candidate c) {
		this.token = token;
		this.candidate = c;
	}

	@Override
	public int getNOCR() {
		return token.getNOCR();
	}

	@Override
	public OCRWord getMasterOCR() {
		return token.getMasterOCR();
	}

	@Override
	public OCRWord getOtherOCR(int i) {
		return token.getOtherOCR(i);
	}

	@Override
	public Optional<String> getGT() {
		return token.getGT();
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.of(candidate);
	}

	@Override
	public List<Candidate> getAllProfilerCandidates() {
		return token.getAllProfilerCandidates();
	}
}
