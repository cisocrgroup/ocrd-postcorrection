package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.profile.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Implements the the "hard" to get right methods to read candidate and ranked tokens.
// Workspace implementations should extend this class to implement basic token parsing
// and profile resetting and let this class handle the rest.
public abstract class AbstractWorkspace implements Workspace {
    private Map<String, OCRTokenReader> candidate = new HashMap<>();
    private Map<String, OCRTokenReader> ranked = new HashMap<>();

    @Override
    final public OCRTokenReader getCandidateTokenReader(String ifg, Profile profile) throws Exception {
        if (!candidate.containsKey(ifg)) {
            List<OCRToken> tokens = new ArrayList<>();
            TokenFilter.filter(getNormalTokenReader(ifg, profile)).forEach(token->{
                assert(TokenFilter.isLong(token));
                assert(TokenFilter.isNonLexical(token));
                token.getCandidates().forEach(candidate -> tokens.add(new CandidateOCRToken(token, candidate)));
            });
            candidate.put(ifg, new OCRTokenReaderImpl(tokens));
        }
        return candidate.get(ifg);
    }

    @Override
    final public OCRTokenReader getRankedTokenReader(String ifg, Profile profile, Rankings rankings) throws Exception {
        if (!ranked.containsKey(ifg)) {
            List<OCRToken> tokens = new ArrayList<>();
            TokenFilter.filter(getNormalTokenReader(ifg, profile)).forEach(token->{
                assert(TokenFilter.isLong(token));
                assert(TokenFilter.isNonLexical(token));
                if (rankings.containsKey(token) && !rankings.get(token).isEmpty()) {
                    tokens.add(new RankingsOCRToken(token, rankings.get(token)));
                }
            });
            ranked.put(ifg, new OCRTokenReaderImpl(tokens));
        }
        return ranked.get(ifg);
    }

    public static class BaseOCRTokenReaderImpl implements BaseOCRTokenReader {
        private final List<BaseOCRToken> words;

        public BaseOCRTokenReaderImpl(List<BaseOCRToken> words) {
            this.words = words;
        }

        @Override
        public List<BaseOCRToken> read() {
            return words;
        }
    }

    public static class OCRTokenReaderImpl implements OCRTokenReader {
        private final List<OCRToken> tokens;

        public OCRTokenReaderImpl(List<OCRToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public List<OCRToken> read() {
            return tokens;
        }
    }
}
