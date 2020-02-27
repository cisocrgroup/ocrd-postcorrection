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

public class TSVLineAlignment {
    private final Path img;
    private List<TSV> tsvs;
    private Lines.Alignment alignment;

    public TSVLineAlignment(Path img) {
        this.img = img;
    }

    public List<BaseOCRToken> align(List<String> extensions) throws Exception {
        List<Path> paths = new ArrayList<>();
        for (String extension : extensions) {
            Optional<Path> maybePath = findPathWithExtension(img.toString(), extension);
            if (!maybePath.isPresent()) {
                throw new Exception("cannot find ocr tsv file with extension: " + extension);
            }
            paths.add(maybePath.get());
        }
        Optional<Path> maybePath = findPathWithExtension(img.toString(), ".gt.txt");
        if (maybePath.isPresent()) {
            paths.add(maybePath.get());
            return align(paths, extensions.size(), true);
        }
        return align(paths, extensions.size(), false);
    }

    private List<BaseOCRToken> align(List<Path> paths, int nOCR, boolean withGT) throws Exception {
        this.tsvs = new ArrayList<>(nOCR);
        for (int i = 0; i < nOCR; i++) {
            this.tsvs.add(TSV.read(paths.get(i)));
        }
        List<String> lines = new ArrayList<>(nOCR);
        for (TSV ll: this.tsvs) {
            lines.add(clean(ll.toString()));
        }
        // simply skip empty or too short lines
        if (lines.isEmpty() || lines.get(0).length() <= 3) {
            return new ArrayList<>();
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
        final List<String> normalizedLines = new ArrayList<>(nOCR);
        final List<List<TSV>> splits = new ArrayList<>(alignment.wordAlignments.size());
        for (int i = 0; i < tsvs.size(); i++) {
            normalizedLines.add(clean(tsvs.get(i).toString()));
            splits.add(tsvs.get(i).split(i, alignment.wordAlignments));
        }

        final List<BaseOCRToken> tokens = new ArrayList<>(alignment.wordAlignments.size());
        final List<TSV> words = new ArrayList<>(nOCR);
        for (int i = 0; i < splits.get(0).size(); i++) { // because of the alignment all split lists have the same length
            String gt = withGT ? Lines.join(alignment.wordAlignments.get(i).alignments.get(nOCR-1)) : null;
            for (List<TSV> split : splits) {
                words.add(split.get(i));
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
