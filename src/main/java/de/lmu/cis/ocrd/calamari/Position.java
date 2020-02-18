package de.lmu.cis.ocrd.calamari;

import java.util.ArrayList;
import java.util.List;

public class Position {
    private List<Char> chars = new ArrayList<>();
    private int globalStart = 0;
    private int globalEnd = 0;
    private int localStart = 0;
    private int localEnd = 0;

    public int getGlobalStart() {
        return globalStart;
    }

    public List<Char> getChars() {
        return chars;
    }

    public int getGlobalEnd() {
        return globalEnd;
    }

    public int getLocalStart() {
        return localStart;
    }

    public int getLocalEnd() {
        return localEnd;
    }
}
