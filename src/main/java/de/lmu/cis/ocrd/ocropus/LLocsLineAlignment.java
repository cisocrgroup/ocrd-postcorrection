package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.align.Lines;
import de.lmu.cis.ocrd.util.Normalizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LLocsLineAlignment {
    private final Path img;
    private List<LLocs> llocs;
    private Lines.Alignment alignment;

    public LLocsLineAlignment(Path img) {
        this.img = img;
    }

    public List<BaseOCRToken> align(int nOCR) throws Exception {
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < nOCR; i++) {
            final String ext = ".llocs." + (i+1);
            Optional<Path> maybePath = findPathWithExtension(img.toString(), ext);
            if (!maybePath.isPresent()) {
                throw new Exception("cannot find ocr llocs file with extension: " + ext);
            }
            paths.add(maybePath.get());
        }
        Optional<Path> maybePath = findPathWithExtension(img.toString(), ".gt.txt");
        if (maybePath.isPresent()) {
            paths.add(maybePath.get());
            return align(paths, nOCR, true);
        }
        return align(paths, nOCR, false);
    }

    private List<BaseOCRToken> align(List<Path> paths, int nOCR, boolean withGT) throws Exception {
        this.llocs = new ArrayList<>(nOCR);
        for (int i = 0; i < nOCR; i++) {
            this.llocs.add(LLocs.read(paths.get(i)));
        }
        List<String> lines = new ArrayList<>(nOCR);
        for (LLocs ll: this.llocs) {
            lines.add(clean(ll.toString()));
        }
        if (withGT) {
            try (BufferedReader r = new BufferedReader(new FileReader(paths.get(nOCR).toFile()))) {
                lines.add(clean(r.readLine()));
            }
        }
        this.alignment = Lines.align(lines.toArray(new String[0]));
        return align(nOCR, withGT);
    }

    private List<BaseOCRToken> align(int nOCR, boolean withGT) throws Exception {
        final List<BaseOCRToken> tokens = new ArrayList<>(alignment.wordAlignments.size());
        final List<String> normalizedLines = new ArrayList<>(nOCR);
        final List<List<LLocs>> splits = new ArrayList<>(alignment.wordAlignments.size());
        for (int i = 0; i < nOCR; i++) {
            normalizedLines.add(clean(llocs.get(i).toString()));
            splits.add(llocs.get(i).split(i, alignment.wordAlignments));
        }

        final List<LLocs> words = new ArrayList<>(nOCR);
        for (int i = 0; i < splits.get(0).size(); i++) { // because of the alignment all split lists have the same length
            String gt = withGT ? Lines.join(alignment.wordAlignments.get(i).alignments.get(nOCR-1)) : null;
            for (int j = 0; j < nOCR; j++) {
                words.add(splits.get(i).get(j));
            }
            tokens.add(new BaseOCRToken(words, normalizedLines, gt));
            words.clear();
        }
        return tokens;
    }

    private static String clean(String dirty) {
        dirty = dirty.replace('$', ' ');
        dirty = dirty.replace('#', ' ');
        return Normalizer.normalize(dirty);
    }

    private static Optional<Path> findPathWithExtension(String base, String ext) {
        for (int pos = base.length(); pos > 0; pos = base.substring(0, pos).lastIndexOf('.')) {
            Path path = Paths.get(base.substring(0, pos)+ext);
            if (path.toFile().exists()) {
                return Optional.of(path);
            }
        }
        return Optional.empty();
    }
}