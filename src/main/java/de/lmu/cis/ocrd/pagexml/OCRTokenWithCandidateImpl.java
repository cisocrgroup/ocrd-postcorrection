package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
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
	public OCRWord getSlaveOCR(int i) {
		return token.getSlaveOCR(i);
	}

	@Override
	public Optional<String> getGT() {
		return token.getGT();
	}

	@Override
	public List<Ranking> getRankings() {
		return new ArrayList<>();
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.of(candidate);
	}

	@Override
	public List<Candidate> getAllProfilerCandidates() {
		return token.getAllProfilerCandidates();
	}

	@Override
	public boolean isLexiconEntry() {
		return candidate.isLexiconEntry();
	}

	@Override
	public boolean ocrIsCorrect() {
		return token.ocrIsCorrect();
	}

	@Override
	public void correct(String correction, double confidence) {
		token.correct(correction, confidence);
	}

	@Override
	public String toString() {
		return String.format("%s,suggestion:%s,hist:%d,ocr:%d", token.toString(), candidate.Suggestion,
				candidate.HistPatterns.length, candidate.OCRPatterns.length);
	}
}
