package de.lmu.cis.ocrd.profile;

public class Candidate {
	public String Suggestion, Modern, Dict;
	public PosPattern[] HistPatterns, OCRPatterns;
	public int Distance;
	public double Weight;

	public Candidate allToLowerCase() {
		this.Suggestion = Suggestion.toLowerCase();
		this.Modern = this.Modern.toLowerCase();
		this.Dict = this.Dict.toLowerCase();
		if (this.HistPatterns != null) {
			for (PosPattern h : this.HistPatterns) {
				h.toLowerCase();
			}
		}
		if (this.OCRPatterns != null) {
			for (PosPattern o : this.OCRPatterns) {
				o.toLowerCase();
			}
		}
		return this;
	}
}
