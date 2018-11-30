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
import java.util.Optional;
import java.util.StringJoiner;


public class AlignCommand implements Command {
    public static class Data {
        public Data() {
            this.pairwise = new ArrayList<>();
            this.tokens = new ArrayList<>();
            this.lines = new ArrayList<>();
        }

        public List<Token> tokens;
        public List<String[]> pairwise;
        public List<String> lines;
    }

    public static class Token {
        public Token(String master) {
            this.master = master;
            this.alignments = new ArrayList<>();
            this.pairwise = new ArrayList<>();
        }

        public String master;
        public List<List<String>> alignments;
        public List<String[]> pairwise;

        public void calculatePairwise() {
            for (List<String> strs : alignments) {
                String other = join(strs);
                Graph g = new Graph(master, other);
                pairwise.add(getPairwise(g.getStartNode()));
            }
        }

        private static String join(List<String> strs) {
            StringJoiner sj = new StringJoiner(" ");
            for (String str : strs) {
                sj.add(str);
            }
            return sj.toString();
        }
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

    public static void main(String[] args) throws IOException {
        final Optional<Integer> n = parseArg(args);
        if (!n.isPresent()) {
            throw new RuntimeException("missing integer argument");
        }
        if (n.get() <= 0) {
            throw new RuntimeException("invalid integer argument: " + n.get());
        }
        align(n.get());
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
        Data data = new Data();
        final String master = NormalizerTransducer.normalize(lines[0]);
        data.lines.add(master);
        final TokenAlignment tokenAlignment = new TokenAlignment(master);
        for (int i = 1; i < lines.length; i++) {
            final String other = NormalizerTransducer.normalize(lines[i]);
            final Graph g = new Graph(master, other);
            final String[] pairwise = getPairwise(g.getStartNode());
            data.lines.add(other);
            data.pairwise.add(pairwise);
            assert (pairwise.length > 1); // #...$
            tokenAlignment.add(other);
        }

        for (TokenAlignment.Token t : tokenAlignment) {
            Token token = new Token(t.getMaster());
            for (int i = 1; i < lines.length; i++) {
                token.alignments.add(t.getAlignment(i - 1));
            }
            token.calculatePairwise();
            data.tokens.add(token);
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

    private static Optional<Integer> parseArg(String[] args) {
        if (args.length != 1) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(args[0]));
    }
}
