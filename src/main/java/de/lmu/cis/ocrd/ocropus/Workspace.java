package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Workspace extends AbstractWorkspace {
    private final Parameters parameters;
    private final Map<String, BaseOCRTokenReader> base = new HashMap<>();
    private final Map<String, OCRTokenReader> normal = new HashMap<>();

    public Workspace(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public BaseOCRTokenReader getBaseOCRTokenReader(String ifg) throws Exception {
        if (!base.containsKey(ifg)) {
            List<Path> imageFiles = gatherImageFiles(Paths.get(ifg));
            imageFiles.sort(Path::compareTo);
            List<de.lmu.cis.ocrd.ml.BaseOCRToken> tokens = new ArrayList<>(imageFiles.size());
            for (Path imageFile: imageFiles) {
                List<BaseOCRToken> lineTokens = new TSVLineAlignment(imageFile).align(parameters.getOcropusOCRExtensions());
                tokens.addAll(lineTokens);
            }
            for (int i = 0; i < tokens.size(); i++) {
                ((BaseOCRToken)tokens.get(i)).setID(i+1);
            }
            base.put(ifg, new AbstractWorkspace.BaseOCRTokenReaderImpl(tokens));
        }
        return base.get(ifg);
    }

    @Override
    public OCRTokenReader getNormalTokenReader(String dir, Profile profile) throws Exception {
        if (!normal.containsKey(dir)) {
            List<OCRToken> tokens = new ArrayList<>();
            getBaseOCRTokenReader(dir).read().forEach(t -> tokens.add(makeCandidateOCRToken(t, profile)));
            normal.put(dir, new AbstractWorkspace.OCRTokenReaderImpl(tokens));
        }
        return normal.get(dir);
    }

    @Override
    public void write(String ifg, String ofg) {
        Logger.debug("writing {} to {}", ifg, ofg);
    }

    @Override
    public void resetProfile(String dir, Profile profile) throws Exception {
        List<OCRToken> tokens = getNormalTokenReader(dir, profile).read();
        for (int i = 0; i < tokens.size(); i++) {
            final de.lmu.cis.ocrd.ml.BaseOCRToken token = ((AbstractOCRToken)tokens.get(i)).getBase();
            tokens.set(i, makeCandidateOCRToken(token, profile));
        }
        normal.put(dir, new AbstractWorkspace.OCRTokenReaderImpl(tokens));
    }

    public Parameters getParameters() {
        return parameters;
    }

    private OCRToken makeCandidateOCRToken(de.lmu.cis.ocrd.ml.BaseOCRToken token, Profile profile) {
        final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
        if (!maybeCandidates.isPresent()) {
            return new CandidatesOCRToken(token, new ArrayList<>());
        }
        List<Candidate> candidates = maybeCandidates.get().Candidates;
        return new CandidatesOCRToken(token, candidates.subList(0, Math.min(candidates.size(), parameters.getMaxCandidates())));
    }

    private List<Path> gatherImageFiles(Path base) throws IOException {
        ImagePathGatherer v = new ImagePathGatherer(parameters.getOcropusImageExtension());
        Files.walkFileTree(base, v);
        v.imageFiles.sort(Comparator.comparing(Path::toString));
        return v.imageFiles;
    }

    private static class ImagePathGatherer extends SimpleFileVisitor<Path> {
        private final String extension;
        private final List<Path> imageFiles;
        private ImagePathGatherer(String extension) {
            this.extension = extension;
            this.imageFiles = new ArrayList<>();
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (!attrs.isRegularFile()) { // skip dirs, links ...
                return FileVisitResult.CONTINUE;
            }
            if (file.toString().endsWith(extension)) {
                imageFiles.add(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
