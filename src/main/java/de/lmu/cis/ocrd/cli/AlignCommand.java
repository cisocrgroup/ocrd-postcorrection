package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.NormalizerTransducer;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.align.Node;
import de.lmu.cis.ocrd.align.TokenAlignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


public class AlignCommand implements Command {
    public static class Data {
        public Data(String masterLine) {
            this.words = new ArrayList<>();
            this.line = new Line(masterLine);
        }

        public List<Word> words;
        public Line line;
    }

    public static class Line {
        public Line(String master) {
            this.master = master;
            this.pairwise = new ArrayList<>();
            this.alignments = new ArrayList<>();
        }

        public String master;
        public List<String> alignments;
        public List<String[]> pairwise;
    }

    public static class Word {
        public Word(String master) {
            this.master = master;
            this.alignments = new ArrayList<>();
            this.pairwise = new ArrayList<>();
        }

        public String master;
        public List<List<String>> alignments;
        public List<String[]> pairwise;
    }

    public static class Parameter {
        public int n;
    }

    @Override
    public void execute(CommandLineArguments args) throws Exception {
        final Parameter p = args.mustGetParameter(Parameter.class);
        if (p.n <= 0) {
            throw new Exception("invalid n: " + p.n);
        }
        align(p.n);
    }

    @Override
    public String getName() {
        return "align";
    }

    private static void align(int n) throws IOException {
        String[] lines = new String[n];
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final List<Data> data = new ArrayList<>();
        while (readLines(br, lines)) {
            data.add(alignLines(lines));
        }
        System.out.println(new Gson().toJson(data));
    }

    private static Data alignLines(String[] lines) {
        assert (lines.length > 0);
        final String master = NormalizerTransducer.normalize(lines[0]);
        Data data = new Data(master);
        final TokenAlignment tokenAlignment = new TokenAlignment(master);
        for (int i = 1; i < lines.length; i++) {
            final String other = NormalizerTransducer.normalize(lines[i]);
            final Graph g = new Graph(master, other);
            final String[] pairwise = getPairwise(g.getStartNode());
            data.line.alignments.add(other);
            data.line.pairwise.add(pairwise);
            assert (pairwise.length > 1); // #...$
            tokenAlignment.add(other);
        }

        for (TokenAlignment.Token t : tokenAlignment) {
            Word word = new Word(t.getMaster());
            for (int i = 1; i < lines.length; i++) {
                List<String> strs = t.getAlignment(i-1);
                word.alignments.add(strs);
                word.pairwise.add(getPairwise(new Graph(word.master, join(strs)).getStartNode()));
            }
            data.words.add(word);
        }
        return data;
    }

    private static boolean readLines(BufferedReader br, String[] lines) throws IOException {
        for (int i = 0; i < lines.length; i++) {
            lines[i] = br.readLine();
            if (lines[i] == null) {
                if (i != 0) {
                    throw new IOException("premature EOF");
                }
                return false;
            }
        }
        return true;
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
