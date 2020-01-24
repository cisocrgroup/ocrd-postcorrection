package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.profile.Profile;

public interface Workspace {
    TokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception;
    TokenReader getCandidateTokenReader(String ifg, Profile profile) throws Exception;
    TokenReader getRankedTokenReader(String ifg, Profile profile, Rankings rankings) throws Exception;
    void resetProfile(String ifg, Profile profile) throws Exception;
    void write(String ifg, String ofg) throws Exception;
    void save() throws Exception;
}
