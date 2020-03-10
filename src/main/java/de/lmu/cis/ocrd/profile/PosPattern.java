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

	// adjusts the position to a fitting position in the given ocr token.
	// return -1 if the right pattern cannot be matched onto the ocr token.
	public int getAdjustedPosition(int[] ocrToken) {
		final int min = Pos >= 2 ? Pos-2 : 0;
		final int max = Math.min(Pos + 2, ocrToken.length);
		final int[] right = Right.codePoints().toArray();
		for (int i = min; i < max; i++) {
			boolean match = true;
			for (int j = 0; j < right.length && (i+j) < ocrToken.length; j++) {
				if (ocrToken[j+i] != right[j]) {
					match = false;
					break;
				}
			}
			if (match) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		return Left + ":" + Right + ":" + Pos;
	}
}
