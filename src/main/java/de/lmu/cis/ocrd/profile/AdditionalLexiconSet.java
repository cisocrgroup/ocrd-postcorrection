package de.lmu.cis.ocrd.profile;

import java.util.HashSet;
import java.util.Set;

public class AdditionalLexiconSet extends HashSet<String> implements AdditionalLexicon {
    @Override
    public boolean use() {
        return true;
    }

    @Override
    public Set<String> entries() {
        return this;
    }

    @Override
    public String toString() {
        return "set_" + size();
    }
}
