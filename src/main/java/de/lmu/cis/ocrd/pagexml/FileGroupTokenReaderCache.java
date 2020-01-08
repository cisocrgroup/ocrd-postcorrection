package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.Ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileGroupTokenReaderCache {
    private final METS mets;
    private Map<OCRToken, List<Ranking>> rankings;
    private final Map<String, TokenReader> normal;
    private final Map<String, TokenReader> candidate;
    private final Map<String, TokenReader> ranked;

    public FileGroupTokenReaderCache(METS mets) {
        this.mets = mets;
        normal = new HashMap<>();
        candidate = new HashMap<>();
        ranked = new HashMap<>();
    }

    public TokenReader getNormalTokenReader(String ifg) {
        if (!normal.containsKey(ifg)) {
            normal.put(ifg, new FileGroupTokenReader(mets, ifg));
        }
        return normal.get(ifg);
    }

    public TokenReader getCandidateTokenReader(String ifg) {
        if (!candidate.containsKey(ifg)) {
            candidate.put(ifg, new FileGroupCandidateTokenReader(getNormalTokenReader(ifg)));
        }
        return candidate.get(ifg);
    }

    public void setRankings(Map<OCRToken, List<Ranking>> rankings) {
        this.rankings = rankings;
    }

    public TokenReader getRankedTokenReader(String ifg) {
        if (!ranked.containsKey(ifg)) {
            ranked.put(ifg, new FileGroupRankingsTokenReader(getNormalTokenReader(ifg), rankings));
        }
        return ranked.get(ifg);
    }
}
