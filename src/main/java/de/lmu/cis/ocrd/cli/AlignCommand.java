package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.util.Normalizer;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.align.Node;
import de.lmu.cis.ocrd.align.TokenAlignment;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


public class AlignCommand extends AbstractIOCommand {
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
            this.raw = new ArrayList<>();
        }

        public String master;
        public List<String> alignments;
	    public List<String[]> pairwise;
	    public List<String> raw;
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

    public AlignCommand() {
        super();
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

    private void align(int n) throws IOException {
        String[] lines = new String[n];
	    final List<Data> data = new ArrayList<>();
	    // read input
        while (readLines(lines)) {
        	data.add(alignLines(lines));
        }
        println(new Gson().toJson(data));
        flush();
    }

    private static Data alignLines(String[] lines) {
        assert (lines.length > 0);
        for (String line : lines) {
        	Logger.info("line: {}", line);
        }
        // final String master = Normalizer.normalize(lines[0]);
        final String master = lines[0];
	    Logger.info("master: {}", master);
        Data data = new Data(master);
        final TokenAlignment tokenAlignment = new TokenAlignment(master);
        for (int i = 1; i < lines.length; i++) {
            // final String other = Normalizer.normalize(lines[i]);
            final String other = lines[i];
	        Logger.info("other: {}", other);
            final Graph g = new Graph(master, other);
            Logger.info("raw: {}", g.getStartNode().toString());
            data.line.raw.add(g.getStartNode().toString());
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

    private boolean readLines(String[] lines) throws IOException {
        for (int i = 0; i < lines.length; i++) {
        	lines[i] = readLine();
            if (lines[i] == null) {
                return false;
            }
            lines[i] = lines[i].trim();
            lines[i] = lines[i].replace('#', ' ');
            lines[i] = lines[i].replace('$', ' ');
            Logger.info("read line: {}", lines[i]);
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
