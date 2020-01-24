package de.lmu.cis.ocrd.align;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Lines {
    public static class Alignment {
        public Alignment(String masterLine) {
            this.wordAlignments = new ArrayList<>();
            this.lineAlignment = new LineAlignment(masterLine);
        }
        public List<WordAlignment> wordAlignments;
        public LineAlignment lineAlignment;
    }

    public static class LineAlignment {
        public LineAlignment(String master) {
            this.master = master;
            this.pairwise = new ArrayList<>();
            this.alignments = new ArrayList<>();
            this.raw = new ArrayList<>();
        }

        public String master;
        public List<String> alignments;
        public List<String[]> pairwise;
        public List<String> raw;
    }

    public static class WordAlignment {
        public WordAlignment(String master) {
            this.master = master;
            this.alignments = new ArrayList<>();
            this.pairwise = new ArrayList<>();
        }

        public String master;
        public List<List<String>> alignments;
        public List<String[]> pairwise;
    }

    public static Alignment align(String[] lines) {
        // final String master = Normalizer.normalize(lines[0]);
        final String master = lines[0];
        Alignment data = new Alignment(master);
        final TokenAlignment tokenAlignment = new TokenAlignment(master);
        for (int i = 1; i < lines.length; i++) {
            // final String other = Normalizer.normalize(lines[i]);
            final String other = lines[i];
            final Graph g = new Graph(master, other);
            data.lineAlignment.raw.add(g.getStartNode().toString());
            final String[] pairwise = getPairwise(g.getStartNode());
            data.lineAlignment.alignments.add(other);
            data.lineAlignment.pairwise.add(pairwise);
            assert (pairwise.length > 1); // #...$
            tokenAlignment.add(other);
        }
        for (TokenAlignment.Token t : tokenAlignment) {
            WordAlignment word = new WordAlignment(t.getMaster());
            for (int i = 1; i < lines.length; i++) {
                List<String> strs = t.getAlignment(i-1);
                word.alignments.add(strs);
                word.pairwise.add(getPairwise(new Graph(word.master, join(strs)).getStartNode()));
            }
            data.wordAlignments.add(word);
        }
        return data;
    }

    private static String[] getPairwise(Node node) {
        final String[] pair = {"", ""};
        while (true) {
            pair[0] += node.getLabel();
            pair[1] += node.getLabel();
            if (node.next(0) == null) {
                break;
            }
            String g0 = node.next(0).getLabel();
            String g1 = node.next(1).getLabel();
            while (g0.length() < g1.length()) {
                g0 += '_';
            }
            while (g1.length() < g0.length()) {
                g1 += '_';
            }
            pair[0] += g0;
            pair[1] += g1;
            node = (Node) node.next(0).next(0);
        }
        pair[0] = pair[0].substring(1, pair[0].length() - 1);
        pair[1] = pair[1].substring(1, pair[1].length() - 1);
        return pair;
    }

    private static String join(List<String> strs) {
        StringJoiner sj = new StringJoiner(" ");
        for (String str : strs) {
            sj.add(str);
        }
        return sj.toString();
    }
}