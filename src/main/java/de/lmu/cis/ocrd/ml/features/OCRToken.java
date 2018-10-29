package de.lmu.cis.ocrd.ml.features;

import java.util.Optional;

import de.lmu.cis.ocrd.profile.Candidate;

public interface OCRToken {
	public OCRWord getMasterOCR();

	public OCRWord getOtherOCR(int i);

	public Optional<String> getGT();

	public Optional<Candidate> getProfilerCandidate();

}
