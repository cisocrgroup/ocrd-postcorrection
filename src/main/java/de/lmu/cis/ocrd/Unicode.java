package de.lmu.cis.ocrd;

public class Unicode {
    public static boolean isLetter(int c) {
        if (Character.isLetter(c)) {
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
    private Unicode() {}
}
