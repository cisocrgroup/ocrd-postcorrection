package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

class METSFileGroupReader {
    private final METS mets;
    private final Parameters parameters;
    private final Map<String, List<Word>> words;
    private final Map<String, List<de.lmu.cis.ocrd.ml.BaseOCRToken>> base;
    private final Map<String, OCRTokenReader> normal;
    private final Map<String, OCRTokenReader> candidate;
    private final Map<String, OCRTokenReader> ranked;

    METSFileGroupReader(METS mets, Parameters parameters) {
        this.mets = mets;
        this.parameters = parameters;
        words = new HashMap<>();
        base = new HashMap<>();
        normal = new HashMap<>();
        candidate = new HashMap<>();
        ranked = new HashMap<>();
    }

    List<Word> readWords(String ifg) throws Exception {
        if (!words.containsKey(ifg)) {
            Logger.debug("reading words for {}", ifg);
            ArrayList<Word> tmp = new ArrayList<>();
            for (METS.File file : mets.findFileGrpFiles(ifg)) {
                try (InputStream is = file.openInputStream()) {
                    final Page page = Page.parse(Paths.get(file.getFLocat()), is);
                    for (Line line : page.getLines()) {
                        tmp.addAll(line.getWords());
                    }
                }
            }
            words.put(ifg, tmp);
            Logger.debug("read {} words for {}", tmp.size(), ifg);
        }
        return words.get(ifg);
    }

    private interface Func {
        void apply(Node word, List<String> linesNormalized);
    }

    private void eachWord(String ifg, Func func) throws Exception {
        for (METS.File file: mets.findFileGrpFiles(ifg)) {
            try (InputStream is = file.openInputStream()) {
                Logger.info("loading page {}", file.getFLocat());
                final Page page = Page.parse(Paths.get(file.getFLocat()), is);
                NodeList nodes = (NodeList) XPathHelper.TEXT_LINES.evaluate(page.getRoot(), XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    final List<String> linesNormalized = new Line(nodes.item(i), page).getUnicodeNormalized();
                    final NodeList words = (NodeList) XPathHelper.CHILD_WORD.evaluate(nodes.item(i), XPathConstants.NODESET);
                    if (linesNormalized.isEmpty() || linesNormalized.get(0).isEmpty()) {
                        continue;
                    }
                    for (int j = 0; j < words.getLength(); j++) {
                        func.apply(words.item(j), linesNormalized);
                    }
                }
                Logger.info("loaded page {}", file.getFLocat());
            }
        }
    }

    BaseOCRTokenReader getBaseOCRTokenReader(String ifg) throws Exception {
        if (!base.containsKey(ifg)) {
            Logger.info("adding base ocr tokens for {}", ifg);
            final List<de.lmu.cis.ocrd.ml.BaseOCRToken> tokens = new ArrayList<>();
            eachWord(ifg, (word, linesNormalized)->{
                try {
                    tokens.add(new BaseOCRToken(word, linesNormalized, parameters.getNOCR()));
                } catch (Exception e) {
                    Logger.warn("cannot add token: {}", e.toString());
                }
            });
            Logger.info("loaded tokens");
            base.put(ifg, tokens);
            Logger.info("added {} base ocr tokens for {}", tokens.size(), ifg);
        }
        return new BaseOCRTokenReaderImpl(base.get(ifg));
    }

    OCRTokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception {
        if (!normal.containsKey(ifg)) {
            updateNormalTokens(ifg, profile);
        }
        return normal.get(ifg);
    }

    private void updateNormalTokens(String ifg, Profile profile) throws Exception {
        final List<OCRToken> tokens = new ArrayList<>();
        for (de.lmu.cis.ocrd.ml.BaseOCRToken token: getBaseOCRTokenReader(ifg).read()) {
            final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
            if (maybeCandidates.isPresent()) {
                final List<Candidate> candidates = maybeCandidates.get().Candidates;
                tokens.add(new CandidatesOCRToken(token, candidates.subList(0, Math.min(candidates.size(), parameters.getMaxCandidates()))));
            } else {
                tokens.add(new CandidatesOCRToken(token));
            }
        }
        normal.put(ifg, new OCRTokenReaderImpl(tokens));
    }

    void setProfile(String ifg, Profile profile) throws Exception {
        normal.remove(ifg);
        updateNormalTokens(ifg, profile); // reset normal tokens with updated profile
    }

    OCRTokenReader getCandidateTokenReader(String ifg, Profile profile) throws Exception {
        if (!candidate.containsKey(ifg)) {
            final List<OCRToken> tokens = new ArrayList<>();
            for (OCRToken token: getNormalTokenReader(ifg, profile).read()) {
                for (Candidate candidate: token.getCandidates()) {
                    tokens.add(new CandidateOCRToken(token, candidate));
                }
            }
            candidate.put(ifg, new OCRTokenReaderImpl(tokens));
        }
        return candidate.get(ifg);
    }

    OCRTokenReader getRankedTokenReader(String ifg, Profile profile, Rankings rankings) throws Exception {
        if (!ranked.containsKey(ifg)) {
            final List<OCRToken> tokens = new ArrayList<>();
            for (OCRToken token: getNormalTokenReader(ifg, profile).read()) {
                if (rankings.containsKey(token)) {
                    tokens.add(new RankingsOCRToken(token, rankings.get(token)));
                }
            }
            ranked.put(ifg, new OCRTokenReaderImpl(tokens));
        }
        return ranked.get(ifg);
    }

    private static class BaseOCRTokenReaderImpl implements BaseOCRTokenReader {
        private final List<de.lmu.cis.ocrd.ml.BaseOCRToken> words;

        BaseOCRTokenReaderImpl(List<de.lmu.cis.ocrd.ml.BaseOCRToken> words) {
            this.words = words;
        }

        @Override
        public List<de.lmu.cis.ocrd.ml.BaseOCRToken> read() {
            return words;
        }
    }

    private static class OCRTokenReaderImpl implements OCRTokenReader {
        private final List<OCRToken> tokens;

        OCRTokenReaderImpl(List<OCRToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public List<OCRToken> read() {
            return tokens;
        }
    }
}
