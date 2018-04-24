package de.lmu.cis.ocrd;

import java.util.ArrayList;
import java.util.List;

import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;

public class PocowebLine extends de.lmu.cis.api.model.Line implements de.lmu.cis.ocrd.Line {
	private class Pair {
		public String normalized;
		public List<Token> tokenAlignements;

		public Pair(String normalized, List<Token> tokenAlignements) {
			this.normalized = normalized;
			this.tokenAlignements = tokenAlignements;
		}
	}

	private final Client client;
	private final List<Token> tokens;
	private final String normalized;
	private final List<Token> tokenAlignements;

	public PocowebLine(Client client, de.lmu.cis.api.model.Line line) throws Exception {
		this.withProjectId(line.getProjectId()).withPageId(line.getLineId()).withLineId(line.getLineId())
				.withOcr(line.getOcr()).withCor(line.getCor()).withCuts(line.getCuts())
				.withConfidences(line.getConfidences()).withAverageConfidence(line.getAverageConfidence())
				.withIsFullyCorrected(line.getIsFullyCorrected())
				.withIsPartiallyCorrected(line.getIsPartiallyCorrected()).withBox(line.getBox());
		this.client = client;
		this.tokens = loadTokens();
		Pair pair = getNormalizedData();
		this.normalized = pair.normalized;
		this.tokenAlignements = pair.tokenAlignements;
	}

	@Override
	public int getLineId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNormalized() {
		return normalized;
	}

	private Pair getNormalizedData() {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		List<Token> alignements = new ArrayList<Token>();
		for (Token t : tokens) {
			if (first) {
				first = false;
			} else {
				builder.append(' ');
				alignements.add(null); // there is surely a better way
			}
			builder.append(t.getCor());
			for (int i = 0; i < t.getCor().length(); i++) {
				alignements.add(t);
			}
		}
		return new Pair(builder.toString(), alignements);
	}

	@Override
	public int getPageId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Token getTokenAt(int i) {
		return tokenAlignements.get(i);
	}

	@Override
	public List<Token> getTokens() {
		return tokens;
	}

	private List<Token> loadTokens() throws Exception {
		List<Token> tokens = client.getTokens(projectId, pageId, lineId);
		if (tokens == null) {
			return new ArrayList<Token>();
		}
		return tokens;
	}

	@Override
	public String toString() {
		return this.getNormalized();
	}
}
