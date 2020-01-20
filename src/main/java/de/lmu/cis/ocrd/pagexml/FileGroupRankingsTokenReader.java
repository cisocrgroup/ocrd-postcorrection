package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.features.Ranking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileGroupRankingsTokenReader implements TokenReader {
    private final TokenReader tokenReader;
    private final Map<OCRToken, List<Ranking>> rankings;
    private List<OCRToken> tokens;

    public FileGroupRankingsTokenReader(TokenReader tokenReader, Map<OCRToken, List<Ranking>> rankings) {
        this.tokenReader = tokenReader;
        this.rankings = rankings;
    }

    @Override
    public List<OCRToken> readTokens() throws Exception {
        if (tokens == null) {
            tokens = readRankingsTokens();
        }
        return tokens;
    }

    private List<OCRToken> readRankingsTokens() throws Exception {
        List<OCRToken> tokens = new ArrayList<>();
        for (OCRToken token : tokenReader.readTokens()) {
            if (!rankings.containsKey(token)) {
                continue;
            }
            tokens.add(new OCRTokenWithRankingsImpl(token, rankings.get(token)));
        }
        return tokens;
    }
}
