package de.lmu.cis.ocrd.profile;

// PosPattern represents a historical rewrite pattern or a OCR error pattern.
// In the case of historical patterns, Left is the modern part and right the historical part.
// For OCR Right is the correct part and left represents the wrong OCR part.
public class PosPattern {
	public int Pos;
	public String Left, Right;

	public PosPattern toLowerCase() {
		this.Left = this.Left.toLowerCase();
		this.Right = this.Right.toLowerCase();
		return this;
	}

	// returns true if the pattern represents a substitution
	// abc:de:1 the OCR substituted abc at pos 1 with de.
	public boolean isSubstitution() {
		return !(Left.isEmpty() && Right.isEmpty());
	}

	// returns if the pattern represents an OCR deletion
	// x::1 the OCR deleted the x at pos 1.
	public boolean isDeletion() {
		return Right.isEmpty();
	}

	// returns if the pattern represents an OCR insertion
	// Pattern: :x:1 the OCR inserted x at pos 1.
	public boolean isInsertion() {
		return Left.isEmpty();
	}

	// getAdjustment returns the positional adjustment for all patterns after this one.
	// E.g. ab:c -> -1, c:ab -> +1, ab:cd -> 0
	int getAdjustment() {
		final int l = Left.codePointCount(0, Left.length());
		final int r = Right.codePointCount(0, Right.length());
		return r - l;
	}

	@Override
	public String toString() {
		return Left + ":" + Right + ":" + Pos;
	}
}
