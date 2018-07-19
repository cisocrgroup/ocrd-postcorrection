package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.NormalizerTransducer;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.align.TokenAlignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.StringJoiner;


public class Align {
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
		while (readLines(br, lines)) {
			alignLines(lines);
		}
	}

	private static void alignLines(String[] lines) {
		assert (lines.length > 0);
		final String master = NormalizerTransducer.normalize(lines[0]);
		final TokenAlignment tokenAlignment = new TokenAlignment(master);
		for (int i = 1; i < lines.length; i++) {
			final String other = NormalizerTransducer.normalize(lines[i]);
			final Graph g = new Graph(master, other);
			final String pairwise = g.getStartNode().toString();
			assert (pairwise.length() > 1); // #...$
			System.out.println(pairwise.replace('|', ':'));
			tokenAlignment.add(other);
		}
		final StringJoiner sj = new StringJoiner(",");
		for (TokenAlignment.Token t : tokenAlignment) {
			sj.add(t.toString().replace('|', ':').replace(',', ' '));
		}
		System.out.println(sj.toString());
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

	private static Optional<Integer> parseArg(String[] args) {
		if (args.length != 1) {
			return Optional.empty();
		}
		return Optional.of(Integer.parseInt(args[0]));
	}
}
