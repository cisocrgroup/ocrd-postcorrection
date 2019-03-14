package de.lmu.cis.ocrd.profile;

import java.util.List;

public class Candidates {
	public String OCR = "";
	public List<Candidate> Candidates;// = new Candidate[0];

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
