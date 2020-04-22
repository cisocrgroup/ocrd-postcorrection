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
    private final Map<String, List<Page>> pages;
    private final Map<String, List<de.lmu.cis.ocrd.ml.BaseOCRToken>> base;
    private final Map<String, OCRTokenReader> normal;

    METSFileGroupReader(METS mets, Parameters parameters) {
        this.mets = mets;
        this.parameters = parameters;
        pages = new HashMap<>();
        base = new HashMap<>();
        normal = new HashMap<>();
    }

    private interface Func {
        void apply(int id, Node word, List<String> linesNormalized);
    }

    List<Page> getPages(String ifg) throws Exception {
        if (!pages.containsKey(ifg)) {
            pages.put(ifg, new ArrayList<>());
            for (METS.File file: mets.findFileGrpFiles(ifg)) {
                Logger.debug("loading page", file.getFLocat());
                try (InputStream is = file.openInputStream()) {
                    final Page page = Page.parse(Paths.get(file.getFLocat()), is);
                    page.setMetsFile(file);
                    pages.get(ifg).add(page);
                }
            }
            pages.get(ifg).sort(Comparator.comparing(lhs -> lhs.getPath().toString()));
        }
        return pages.get(ifg);
    }

    private void eachWord(String ifg, Func func) throws Exception {
        int id = 1;
        for (Page page: getPages(ifg)) {
            NodeList nodes = (NodeList) XPathHelper.TEXT_LINES.evaluate(page.getRoot(), XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                final List<String> linesNormalized = new Line(nodes.item(i), page).getUnicodeNormalized();
                if (linesNormalized.isEmpty() || linesNormalized.get(0).isEmpty()) {
                    continue;
                }
                final NodeList words = (NodeList) XPathHelper.CHILD_WORD.evaluate(nodes.item(i), XPathConstants.NODESET);
                for (int j = 0; j < words.getLength(); j++) {
                    func.apply(id++, words.item(j), linesNormalized);
                    // stop after max tokens
                    if (parameters.getMaxTokens() > 0 && id > parameters.getMaxTokens()) {
                        return;
                    }
                }
            }
        }
    }

    BaseOCRTokenReader getBaseOCRTokenReader(String ifg) throws Exception {
        if (!base.containsKey(ifg)) {
            Logger.debug("reading base ocr tokens for input file group {}", ifg);
            final List<de.lmu.cis.ocrd.ml.BaseOCRToken> tokens = new ArrayList<>();
            eachWord(ifg, (id, word, linesNormalized)->{
                try {
                    tokens.add(new BaseOCRToken(id, word, linesNormalized));
                } catch (Exception e) {
                    Logger.warn("cannot add token: {}", e.toString());
                }
            });
            base.put(ifg, tokens);
            Logger.info("read {} base ocr tokens for input file group {}", tokens.size(), ifg);
        }
        return new AbstractWorkspace.BaseOCRTokenReaderImpl(base.get(ifg));
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
        normal.put(ifg, new AbstractWorkspace.OCRTokenReaderImpl(tokens));
    }

    void setProfile(String ifg, Profile profile) throws Exception {
        normal.remove(ifg);
        updateNormalTokens(ifg, profile); // reset normal tokens with updated profile
    }
}
