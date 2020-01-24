package de.lmu.cis.ocrd.ocropus;

import de.lmu.cis.ocrd.align.Lines;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLocs {
    public static class Pair {
        private final int c;
        private final double conf;

        Pair(int c, double conf) {
            this.c = c;
            this.conf = conf;
        }

        public int getC() {
            return c;
        }

        public double getConfidence() {
            return conf;
        }

        private static final Pattern P = Pattern.compile("(.)\t([0-9]*\\.?[0-9]*)\t([0-9]*\\.?[0-9]*)");
        static Pair scan(String line) throws Exception {
            final Matcher m = P.matcher(line);
            if (!m.matches()) {
                throw new Exception("invalid llocs line: " + line);
            }
            final int c = m.group(1).codePointAt(0);
            final double confidence = Double.parseDouble(m.group(3));
            return new Pair(c, confidence);
        }
    }

    private List<Pair> pairs;
    private Path path;
    private String string;

    private LLocs(List<Pair> pairs) {
        this(pairs, Paths.get(""));
    }

    private LLocs(List<Pair> pairs, Path path) {
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

    public static LLocs read(Path path) throws Exception {
        try (InputStream is = new FileInputStream(path.toFile())) {
            LLocs llocs = read(is);
            llocs.path = path;
            return llocs;
        }
    }

    public static LLocs read(InputStream is) throws Exception {
        LLocs ret = new LLocs(new ArrayList<>());
        Scanner scanner = new Scanner(is);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            ret.pairs.add(Pair.scan(line));
        }
        return ret;
    }

    public List<LLocs> split(int index, List<Lines.WordAlignment> wordAlignments) throws Exception {
        int offset = 0;
        List<LLocs> ret = new ArrayList<>();

        for (Lines.WordAlignment wordAlignment : wordAlignments) {
            // handle master
            if (index == 0) {
                final int pos = mustGetIndexOf(wordAlignment.master, offset);
                offset += pos + wordAlignment.master.length();
                ret.add(sublist(pos, offset));
            } else { // alignment: index > 0
                int start = -1;
                for (String part : wordAlignment.alignments.get(index - 1)) {
                    final int pos = mustGetIndexOf(part, offset);
                    if (start == -1) {
                        start = pos;
                    }
                    offset += pos + part.length();
                }
                ret.add(sublist(start, offset));
            }
        }
        return ret;
    }

    private int mustGetIndexOf(String needle, int offset) throws Exception {
        final int pos = toString().substring(offset).indexOf(needle);
        if (pos < 0) {
            throw new Exception("cannot find alignment in llocs: " + needle);
        }
        return pos;
    }

    private LLocs sublist(int start, int end) {
        int a = toString().codePointCount(0, start);
        int e = toString().codePointCount(start, end);
        return new LLocs(this.pairs.subList(a, e), this.path);
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
