package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.profile.Candidate;

import java.util.List;
import java.util.Optional;

public interface OCRToken {
	OCRWord getMasterOCR();
	OCRWord getOtherOCR(int i);
	Optional<String> getGT();
	Optional<Candidate> getProfilerCandidate();
	List<Candidate> getAllProfilerCandidates();
}
