package de.lmu.cis.ocrd;

public class Unicode {
    public static boolean isLetter(int c) {
        if (Character.isLetter(c)) {
            return true;
        }
        switch (Character.getType(c)) {
            case Character.COMBINING_SPACING_MARK:
            // case Character.DIRECTIONALITY_NONSPACING_MARK: /* seems to be the same? */
                return true;
            default:
                return false;
        }
    }
    private Unicode() {}
}
