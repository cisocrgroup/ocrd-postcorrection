package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Workspace {
    private final Parameters parameters;
    private final Map<String, TokenReader> normal = new HashMap<>();
    private final Map<String, TokenReader> candidates = new HashMap<>();
    private final Map<String, TokenReader> ranked = new HashMap<>();

    public Workspace(Parameters parameters) {
        this.parameters = parameters;
    }

    public TokenReader getNormalTokenReader(String dir, Profile profile) throws Exception {
        if (!normal.containsKey(dir)) {
            List<Path> imageFiles = gatherImageFiles(Paths.get(dir));
            List<OCRToken> tokens = new ArrayList<>();
            for (Path imageFile: imageFiles) {
                List<BaseOCRToken> lineTokens = new LLocsLineAlignment(Paths.get(dir)).align(parameters.getNOCR());
                lineTokens.forEach(t->tokens.add(makeCandidateOCRToken(t, profile)));
            }
            normal.put(dir, new TokenReaderImpl(tokens));
        }
        return normal.get(dir);
    }

    public TokenReader getCandidateTokenReader(String dir, Profile profile) throws Exception {
        if (!candidates.containsKey(dir)) {
            List<OCRToken> tokens = new ArrayList<>();
            getNormalTokenReader(dir, profile).readTokens().forEach(t-> t.getCandidates().forEach(c->tokens.add(new CandidateOCRToken(t, c))));
            candidates.put(dir, new TokenReaderImpl(tokens));
        }
        return candidates.get(dir);
    }

    public TokenReader getRankedTokenReader(String dir, Profile profile, Rankings rankings) throws Exception {
        if (!ranked.containsKey(dir)) {
            List<OCRToken> tokens = new ArrayList<>();
            getNormalTokenReader(dir, profile).readTokens().forEach(t-> t.getCandidates().forEach(c->tokens.add(new RankingsOCRToken(t, rankings.get(t)))));
            ranked.put(dir, new TokenReaderImpl(tokens));
        }
        return ranked.get(dir);
    }

    public void resetProfile(String dir, Profile profile) throws Exception {
        List<OCRToken> tokens = getNormalTokenReader(dir, profile).readTokens();
        for (int i = 0; i < tokens.size(); i++) {
            tokens.add(i, makeCandidateOCRToken(((AbstractOCRToken)tokens.get(i)).getBase(), profile));
        }
        normal.put(dir, new TokenReaderImpl(tokens));
    }

    private OCRToken makeCandidateOCRToken(de.lmu.cis.ocrd.ml.BaseOCRToken token, Profile profile) {
        final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
        if (!maybeCandidates.isPresent()) {
            return new CandidatesOCRToken(token, new ArrayList<>());
        }
        List<Candidate> candidates = maybeCandidates.get().Candidates;
        return new CandidatesOCRToken(token, candidates.subList(0, Math.min(candidates.size(), parameters.getMaxCandidates())));
    }

    private static List<Path> gatherImageFiles(Path base) throws IOException {
        ImagePathGatherer v = new ImagePathGatherer();
        Files.walkFileTree(base, v);
        v.imageFiles.sort(Comparator.comparing(Path::toString));
        return v.imageFiles;
    }

    private static class ImagePathGatherer extends SimpleFileVisitor<Path> {
        private final List<Path> imageFiles = new ArrayList<>();
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (!attrs.isRegularFile()) { // skip dirs, links ...
                return FileVisitResult.CONTINUE;
            }
            if (file.toString().endsWith(".png") && !(file.toString().endsWith(".dew.png") || file.toString().endsWith(".bin.png"))) {
                imageFiles.add(file);
            }
            return FileVisitResult.CONTINUE;
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
