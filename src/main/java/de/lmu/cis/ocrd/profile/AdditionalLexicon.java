package de.lmu.cis.ocrd.profile;

import java.util.Set;

public interface AdditionalLexicon {
    boolean use();
    Set<String> entries() throws Exception;
}
