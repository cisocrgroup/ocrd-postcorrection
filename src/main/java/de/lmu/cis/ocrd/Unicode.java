package de.lmu.cis.ocrd;

public class Unicode {
	private Unicode() {
	}

	public static boolean isLetter(int c) {
		if (Character.isLetterOrDigit(c)) {
			return true;
		}
		switch (Character.getType(c)) {
			case Character.COMBINING_SPACING_MARK:
			case Character.NON_SPACING_MARK:
				return true;
			default:
				return false;
		}
	}

	public static boolean isSpace(int c) {
		return Character.isSpaceChar(c);
	}
}
