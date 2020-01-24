package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.align.Lines;

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

    public void align(int nOCR) throws Exception {
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < nOCR; i++) {
            final String ext = ".llocs." + (i+1);
            Optional<Path> maybePath = findPathWithExtension(img.toString(), ext);
            if (!maybePath.isPresent()) {
                throw new Exception("cannot find ocr llocs file with extension: " + ext);
            }
        }
        Optional<Path> maybePath = findPathWithExtension(img.toString(), ".gt.txt");
        if (maybePath.isPresent()) {
            paths.add(maybePath.get());
            align(paths, nOCR, true);
        } else {
            align(paths, nOCR, false);
        }
    }

    private void align(List<Path> paths, int nOCR, boolean withGT) throws Exception {
        this.llocs = new ArrayList<>(nOCR);
        for (int i = 0; i < nOCR; i++) {
            this.llocs.add(LLocs.read(paths.get(i)));
        }
        List<String> lines = new ArrayList<>(nOCR);
        for (LLocs ll: this.llocs) {
            lines.add(ll.toString());
        }
        if (withGT) {
            try (BufferedReader r = new BufferedReader(new FileReader(paths.get(nOCR).toFile()))) {
                lines.add(r.readLine());
            }
        }
        this.alignment = Lines.align(lines.toArray(new String[0]));
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
