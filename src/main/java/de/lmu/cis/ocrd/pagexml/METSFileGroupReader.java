package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.util.*;

public class METSFileGroupReader {
    private final METS mets;
    private final Parameters parameters;
    private Map<OCRToken, List<Ranking>> rankings;
    private final Map<String, WordReader> words;
    private final Map<String, List<BaseOCRToken>> base;
    private final Map<String, TokenReader> normal;
    private final Map<String, TokenReader> candidate;
    private final Map<String, TokenReader> ranked;

    public METSFileGroupReader(METS mets, Parameters parameters) {
        this.mets = mets;
        this.parameters = parameters;
        words = new HashMap<>();
        base = new HashMap<>();
        normal = new HashMap<>();
        candidate = new HashMap<>();
        ranked = new HashMap<>();
    }

    public WordReader getWordReader(String ifg) throws Exception {
        if (!words.containsKey(ifg)) {
            final METSFileGroupWordReader tr = new METSFileGroupWordReader(mets, parameters, ifg);
            words.put(ifg, new WordReaderImpl(tr.readWords()));
        }
        return words.get(ifg);
    }

    private List<BaseOCRToken> getBase(String ifg) throws Exception {
        if (!base.containsKey(ifg)) {
            final List<BaseOCRToken> tokens = new ArrayList<>();
            for (Word word: getWordReader(ifg).readWords()) {
                tokens.add(new BaseOCRToken(word, parameters.getNOCR()));
            }
            base.put(ifg, tokens);
        }
        return base.get(ifg);
    }

    public TokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception {
        if (!normal.containsKey(ifg)) {
            updateNormalTokens(ifg, profile);
        }
        return normal.get(ifg);
    }

    private void updateNormalTokens(String ifg, Profile profile) throws Exception {
        final List<OCRToken> tokens = new ArrayList<>();
        for (BaseOCRToken token: getBase(ifg)) {
            final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
            if (maybeCandidates.isPresent()) {
                final List<Candidate> candidates = maybeCandidates.get().Candidates;
                tokens.add(new CandidatesOCRToken(token, candidates.subList(0, Math.min(candidates.size(), parameters.getMaxCandidates()))));
            } else {
                tokens.add(new CandidatesOCRToken(token));
            }
        }
        normal.put(ifg, new TokenReaderImpl(tokens));
    }

    public void setProfile(String ifg, Profile profile) throws Exception {
        normal.remove(ifg);
        updateNormalTokens(ifg, profile); // reset normal tokens with updated profile
    }

    public TokenReader getCandidateTokenReader(String ifg, Profile profile) throws Exception {
        if (!candidate.containsKey(ifg)) {
            final List<OCRToken> tokens = new ArrayList<>();
            for (OCRToken token: getNormalTokenReader(ifg, profile).readTokens()) {
                for (Candidate candidate: token.getCandidates()) {
                    tokens.add(new CandidateOCRToken(token, candidate));
                }
            }
            candidate.put(ifg, new TokenReaderImpl(tokens));
        }
        return candidate.get(ifg);
    }

    public TokenReader getRankedTokenReader(String ifg, Profile profile, Map<OCRToken, List<Ranking>> rankings) throws Exception {
        if (!ranked.containsKey(ifg)) {
            final List<OCRToken> tokens = new ArrayList<>();
            for (OCRToken token: getNormalTokenReader(ifg, profile).readTokens()) {
                if (rankings.containsKey(token)) {
                    tokens.add(new RankingsOCRToken(token, rankings.get(token)));
                }
            }
            ranked.put(ifg, new TokenReaderImpl(tokens));
        }
        return ranked.get(ifg);
    }

    private static class WordReaderImpl implements WordReader {
        private final List<Word> words;

        WordReaderImpl(List<Word> words) {
            this.words = words;
        }

        @Override
        public List<Word> readWords() {
            return words;
        }
    }

    private static class TokenReaderImpl implements TokenReader {
        private final List<OCRToken> tokens;

        TokenReaderImpl(List<OCRToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public List<OCRToken> readTokens() {
            return tokens;
        }
    }
}
