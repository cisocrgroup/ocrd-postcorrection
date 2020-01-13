package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileGroupTokenReaderCache {
    private final METS mets;
    private final Parameters parameters;
    private Map<OCRToken, List<Ranking>> rankings;
    private final Map<String, FileGroupTokenReader> normal;
    private final Map<String, TokenReader> candidate;
    private final Map<String, TokenReader> ranked;

    public FileGroupTokenReaderCache(METS mets, Parameters parameters) {
        this.mets = mets;
        this.parameters = parameters;
        normal = new HashMap<>();
        candidate = new HashMap<>();
        ranked = new HashMap<>();
    }

    public WordReader getWordReader(String ifg) throws Exception {
        if (!normal.containsKey(ifg)) {
            final FileGroupTokenReader tr = new FileGroupTokenReader(mets, parameters, ifg);
            normal.put(ifg, tr);
        }
        return normal.get(ifg);
    }

    public TokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception {
        if (!normal.containsKey(ifg)) {
            normal.put(ifg, new FileGroupTokenReader(mets, parameters, ifg));
        }
        if (profile != null) {
            normal.get(ifg).setProfile(profile);
        }
        return normal.get(ifg);
    }

    public TokenReader getCandidateTokenReader(String ifg, Profile profile) throws Exception {
        if (!candidate.containsKey(ifg)) {
            candidate.put(ifg, new FileGroupCandidateTokenReader(getNormalTokenReader(ifg, profile)));
        }
        return candidate.get(ifg);
    }

    public TokenReader getRankedTokenReader(String ifg, Profile profile, Map<OCRToken, List<Ranking>> rankings) throws Exception {
        if (!ranked.containsKey(ifg)) {
            ranked.put(ifg, new FileGroupRankingsTokenReader(getNormalTokenReader(ifg, profile), rankings));
        }
        return ranked.get(ifg);
    }
}
