package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.align.Lines;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.pmw.tinylog.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TSV {
    public static class Pair {
        private final int c;
        private final double conf;

        Pair(int c, double conf) {
            this.c = c;
            this.conf = conf;
        }

        public int getChar() {
            return c;
        }

        public double getConfidence() {
            return conf;
        }

        private static final Pattern P = Pattern.compile("^([^\t]*)\t([0-9]*\\.?[0-9]*)$");
        static Pair scan(String line) throws Exception {
            final Matcher m = P.matcher(line);
            if (!m.matches()) {
                throw new Exception("invalid tsv line: " + line);
            }
            final int c = m.group(1).isEmpty() ? (int)' ' : m.group(1).codePointAt(0);
            final double confidence = Double.parseDouble(m.group(2));
            return new Pair(c, confidence);
        }
    }

    private final List<Pair> pairs;
    private Path path;
    private String string;

    private TSV(List<Pair> pairs) {
        this(pairs, Paths.get(""));
    }

    private TSV(List<Pair> pairs, Path path) {
        this.pairs = pairs;
        this.path = path;
    }

    public Pair at(int index) {
        return pairs.get(index);
    }

    public int length() {
        return pairs.size();
    }

    public Path getPath() {
        return path;
    }

    double getAverageConfidence() {
        double sum = 0;
        for (Pair pair: pairs) {
            sum += pair.getConfidence();
        }
        return length() == 0 ? 0 : sum/(double)length();
    }

    public static TSV read(Path path) throws Exception {
        try (InputStream is = new FileInputStream(path.toFile())) {
            TSV tsv = read(is);
            tsv.path = path;
            return tsv;
        }
    }

    public static TSV read(InputStream is) throws Exception {
        TSV tsv = new TSV(new ArrayList<>());
        tsv.path = Paths.get("");
        Scanner scanner = new Scanner(is);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            tsv.pairs.add(Pair.scan(line));
        }
        return tsv;
    }

    public List<TSV> split(int index, List<Lines.WordAlignment> wordAlignments) throws Exception {
        int offset = 0;
        List<TSV> ret = new ArrayList<>();
        for (Lines.WordAlignment wordAlignment : wordAlignments) {
            // handle master
            if (index == 0) {
                final int pos = mustGetIndexOf(wordAlignment.master, offset);
                offset = pos + wordAlignment.master.length();
                ret.add(sublist(pos, offset));
            } else { // alignment: index > 0
                int start = -1;
                for (String part : wordAlignment.alignments.get(index - 1)) {
                    final int pos = mustGetIndexOf(part, offset);
                    if (start == -1) {
                        start = pos;
                    }
                    offset = pos + part.length();
                }
                ret.add(sublist(start, offset));
            }
        }
        return ret;
    }

    private static int exactSearch(String needle, String haystack, int offset, int len) {
        int pos = haystack.indexOf(needle, offset);
        if (pos >= 0 && ((pos-(offset+len)) <= 2*len)) {
            return pos;
        }
        int start = offset - len;
        if (start < 0) {
            start = 0;
        }
        return haystack.indexOf(needle, start);
    }

    private static int levSearch(String needle, String haystack, int offset, int len, int k) {
        LevenshteinDistance lev = new LevenshteinDistance(k);
        final int from = offset >= len ? offset-len : 0;
        final int to = Math.min(offset + len, haystack.length());
        int argMin = -1;
        int min = len;
        for (int i = from; i < to; i++) {
            for (int j = to; j > i; j--) {
                final String substring = haystack.substring(i, j);
                final int dist = lev.apply(substring, needle);
                if (dist < min) {
                    min = dist;
                    argMin = i;
                }
            }
        }
        return argMin;
    }

    private int mustGetIndexOf(String needle, int offset) throws Exception {
        final String str = toString();
        // Use a minimum length of 3.
        int len = needle.length();
        if (len < 3) {
            len = 3;
        }
        // Exact match
        int pos = exactSearch(needle, str, offset, len);
        if (pos >= 0) {
            return pos;
        }

        // We have some problem with the alignment.
        // We search around the offset position for the best match using levenshtein distance.
        pos = levSearch(needle, str, offset, len, 3);
        if (pos >= 0) {
            return pos;
        }

        // Try again with more space
        len *= 2;
        pos = exactSearch(needle, str, offset, len);
        if (pos >= 0) {
            return pos;
        }
        pos = levSearch(needle, str, offset, len, 3);
        if (pos >= 0) {
            return pos;
        }
        // sigh. we just cannot get the index
        throw new Exception("cannot find '" + needle + "' in '" + str + "'" + " at index " + offset);
    }

    private TSV sublist(int start, int end) {
        Logger.debug("sublist({},{})", start, end);
        Logger.debug("string = {}", toString());
        int s = toString().codePointCount(0, start);
        Logger.debug("s = {}", s);
        int len = toString().codePointCount(start, Math.min(end, toString().length()));
        Logger.debug("len = {}", len);
        return new TSV(this.pairs.subList(s, s+len), this.path);
    }

    @Override
    public String toString() {
        if (string == null) {
            StringBuilder sb = new StringBuilder();
            pairs.forEach(pair -> sb.appendCodePoint(pair.c));
            string = sb.toString();
        }
        return string;
    }
}
