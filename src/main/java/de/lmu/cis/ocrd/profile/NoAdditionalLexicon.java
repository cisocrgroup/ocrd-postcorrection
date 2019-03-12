package de.lmu.cis.ocrd.profile;

import java.util.Set;

public class NoAdditionalLexicon implements AdditionalLexicon {
    @Override
    public boolean use() {
        return false;
    }

    @Override
    public Set<String> entries() throws Exception {
        throw new Exception("no entries for the additional lexicon");
    }
}
