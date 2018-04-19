package de.lmu.cis.ocrd.profile;

public class PosPattern {
	public int Pos;
	public String Left, Right;

	public PosPattern toLowerCase() {
		this.Left = this.Left.toLowerCase();
		this.Right = this.Right.toLowerCase();
		return this;
	}
}
