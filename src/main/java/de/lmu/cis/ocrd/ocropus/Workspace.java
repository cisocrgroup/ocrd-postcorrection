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

public class Workspace implements de.lmu.cis.ocrd.ml.Workspace {
    private final Parameters parameters;
    private final Map<String, BaseOCRTokenReader> base = new HashMap<>();
    private final Map<String, OCRTokenReader> normal = new HashMap<>();
    private final Map<String, OCRTokenReader> candidates = new HashMap<>();
    private final Map<String, OCRTokenReader> ranked = new HashMap<>();

    public Workspace(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public BaseOCRTokenReader getBaseOCRTokenReader(String ifg) throws Exception {
        if (!base.containsKey(ifg)) {
            List<Path> imageFiles = gatherImageFiles(Paths.get(ifg));
            List<de.lmu.cis.ocrd.ml.BaseOCRToken> tokens = new ArrayList<>();
            for (Path imageFile: imageFiles) {
                List<BaseOCRToken> lineTokens = new LLocsLineAlignment(Paths.get(ifg)).align(parameters.getNOCR());
                tokens.addAll(lineTokens);
            }
            base.put(ifg, new BaseOCRTokenReaderImpl(tokens));
        }
        return base.get(ifg);
    }

    @Override
    public OCRTokenReader getNormalTokenReader(String dir, Profile profile) throws Exception {
        if (!normal.containsKey(dir)) {
            List<OCRToken> tokens = new ArrayList<>();
            getBaseOCRTokenReader(dir).read().forEach(t -> tokens.add(makeCandidateOCRToken(t, profile)));
            normal.put(dir, new OCRTokenReaderImpl(tokens));
        }
        return normal.get(dir);
    }

    @Override
    public OCRTokenReader getCandidateTokenReader(String dir, Profile profile) throws Exception {
        if (!candidates.containsKey(dir)) {
            List<OCRToken> tokens = new ArrayList<>();
            getNormalTokenReader(dir, profile).read().forEach(t-> t.getCandidates().forEach(c->tokens.add(new CandidateOCRToken(t, c))));
            candidates.put(dir, new OCRTokenReaderImpl(tokens));
        }
        return candidates.get(dir);
    }

    @Override
    public OCRTokenReader getRankedTokenReader(String dir, Profile profile, Rankings rankings) throws Exception {
        if (!ranked.containsKey(dir)) {
            List<OCRToken> tokens = new ArrayList<>();
            getNormalTokenReader(dir, profile).read().forEach(t-> t.getCandidates().forEach(c->tokens.add(new RankingsOCRToken(t, rankings.get(t)))));
            ranked.put(dir, new OCRTokenReaderImpl(tokens));
        }
        return ranked.get(dir);
    }

    @Override
    public void write(String ifg, String ofg) throws Exception {
        throw new Exception("write: not implemented");
    }

    @Override
    public void resetProfile(String dir, Profile profile) throws Exception {
        List<OCRToken> tokens = getNormalTokenReader(dir, profile).read();
        for (int i = 0; i < tokens.size(); i++) {
            tokens.add(i, makeCandidateOCRToken(((AbstractOCRToken)tokens.get(i)).getBase(), profile));
        }
        normal.put(dir, new OCRTokenReaderImpl(tokens));
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

    private static class BaseOCRTokenReaderImpl implements BaseOCRTokenReader {
        private final List<de.lmu.cis.ocrd.ml.BaseOCRToken> tokens;

        BaseOCRTokenReaderImpl(List<de.lmu.cis.ocrd.ml.BaseOCRToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public List<de.lmu.cis.ocrd.ml.BaseOCRToken> read() {
            return tokens;
        }
    }
}
