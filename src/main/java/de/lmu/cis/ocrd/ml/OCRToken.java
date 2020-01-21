package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.List;
import java.util.Optional;

// Main interface for ocr tokens
public interface OCRToken extends BaseOCRToken {
	// Get the rankings for the token.  If there are none, returns an
	// empty list.
	List<Ranking> getRankings();
	// Get Candidate of an RR ocr token.
	Optional<Candidate> getCandidate();
	// Get list of candidates.  If there are none, returns an empty list.
	List<Candidate> getCandidates();
}
