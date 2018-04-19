package de.lmu.cis.ocrd.profile;

public class Candidates {
	public String OCR;
	public Candidate[] Candidates;

	public Candidates toLowerCase() {
		this.OCR = this.OCR.toLowerCase();
		if (this.Candidates == null) {
			return this;
		}
		for (Candidate c : this.Candidates) {
			c.allToLowerCase();
		}
		return this;
	}
}
