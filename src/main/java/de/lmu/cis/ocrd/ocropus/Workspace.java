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
            Logger.debug("found {} line snippets in {}", imageFiles.size(), ifg);
            for (Path imageFile: imageFiles) {

                List<BaseOCRToken> lineTokens = new TSVLineAlignment(imageFile).align(parameters.getOcropusOCRExtensions());
                setTokenIDs(lineTokens, imageFile);
                tokens.addAll(lineTokens);
                if (parameters.getMaxTokens() > 0 && parameters.getMaxTokens() <= tokens.size()) {
                    break;
                }
            }
            Logger.debug("read {} tokens [max={}]", tokens.size(), parameters.getMaxTokens());
            base.clear();
            base.put(ifg, new AbstractWorkspace.BaseOCRTokenReaderImpl(tokens));
        }
        return base.get(ifg);
    }

    private void setTokenIDs(List<BaseOCRToken> tokens, Path imageFile) {
        String file = imageFile.getFileName().toString();
        file = file.substring(0, file.length() - parameters.getOcropusImageExtension().length());
        file = file + parameters.getOcropusOCRExtensions().get(0);
        for (int i = 0; i < tokens.size(); i++) {
            tokens.get(i).setID(file + ":" + (i+1));
        }
    }

    @Override
    public OCRTokenReader getNormalTokenReader(String dir, Profile profile) throws Exception {
        if (!normal.containsKey(dir)) {
            List<OCRToken> tokens = new ArrayList<>();
            getBaseOCRTokenReader(dir).read().forEach(t -> tokens.add(makeCandidateOCRToken(t, profile)));
            normal.clear();
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
            return new CandidatesOCRToken(token);
        }
        List<Candidate> candidates = maybeCandidates.get().Candidates;
        return new CandidatesOCRToken(token, parameters.getMaxCandidates(), maybeCandidates.get().Candidates);
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
